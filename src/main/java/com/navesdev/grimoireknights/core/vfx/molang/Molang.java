package com.navesdev.grimoireknights.core.vfx.molang;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * The slice of Bedrock's Molang the mod's effects use (docs/adr/0004-bedrock-particles.md): numbers, arithmetic,
 * comparison, {@code && || !}, {@code ?:}, common {@code math.*} (angles in degrees) and {@code variable.*} /
 * {@code query.*} read from a {@link Scope}. Anything else fails at parse, so a bad effect breaks on load.
 */
public final class Molang {
    /** Where an expression reads its variables and random numbers. Unset variables are 0, as in Molang. */
    public interface Scope {
        double get(String name);

        /** Uniform in [0, 1). */
        double random();
    }

    @FunctionalInterface
    public interface Expr {
        double eval(Scope scope);
    }

    private record Fn(int arity, Function<double[], Double> body) {}

    private static final Map<String, Fn> FUNCTIONS = Map.ofEntries(
            Map.entry("abs", new Fn(1, a -> Math.abs(a[0]))),
            Map.entry("sin", new Fn(1, a -> Math.sin(Math.toRadians(a[0])))),
            Map.entry("cos", new Fn(1, a -> Math.cos(Math.toRadians(a[0])))),
            Map.entry("sqrt", new Fn(1, a -> Math.sqrt(a[0]))),
            Map.entry("floor", new Fn(1, a -> Math.floor(a[0]))),
            Map.entry("ceil", new Fn(1, a -> Math.ceil(a[0]))),
            Map.entry("round", new Fn(1, a -> (double) Math.round(a[0]))),
            Map.entry("trunc", new Fn(1, a -> a[0] < 0 ? Math.ceil(a[0]) : Math.floor(a[0]))),
            Map.entry("exp", new Fn(1, a -> Math.exp(a[0]))),
            Map.entry("ln", new Fn(1, a -> Math.log(a[0]))),
            Map.entry("pow", new Fn(2, a -> Math.pow(a[0], a[1]))),
            Map.entry("min", new Fn(2, a -> Math.min(a[0], a[1]))),
            Map.entry("max", new Fn(2, a -> Math.max(a[0], a[1]))),
            Map.entry("mod", new Fn(2, a -> a[0] % a[1])),
            Map.entry("clamp", new Fn(3, a -> Math.max(a[1], Math.min(a[2], a[0])))),
            Map.entry("lerp", new Fn(3, a -> a[0] + (a[1] - a[0]) * a[2])));

    private Molang() {}

    public static Expr constant(double value) {
        return scope -> value;
    }

    public static Expr parse(String source) {
        Parser parser = new Parser(source.toLowerCase(Locale.ROOT));
        Expr expr = parser.ternary();
        parser.skipSpace();
        if (parser.pos < parser.src.length()) throw parser.error("unexpected '" + parser.src.charAt(parser.pos) + "'");
        return expr;
    }

    private static final class Parser {
        private final String src;
        private int pos;

        Parser(String src) {
            this.src = src;
        }

        IllegalArgumentException error(String what) {
            return new IllegalArgumentException("Molang: " + what + " at " + pos + " in \"" + src + "\"");
        }

        void skipSpace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }

        boolean eat(String token) {
            skipSpace();
            if (!src.startsWith(token, pos)) return false;
            pos += token.length();
            return true;
        }

        Expr ternary() {
            Expr cond = or();
            if (!eat("?")) return cond;
            Expr yes = ternary();
            if (!eat(":")) throw error("expected ':'");
            Expr no = ternary();
            return s -> cond.eval(s) != 0 ? yes.eval(s) : no.eval(s);
        }

        Expr or() {
            Expr left = and();
            while (eat("||")) {
                Expr l = left, r = and();
                left = s -> l.eval(s) != 0 || r.eval(s) != 0 ? 1 : 0;
            }
            return left;
        }

        Expr and() {
            Expr left = comparison();
            while (eat("&&")) {
                Expr l = left, r = comparison();
                left = s -> l.eval(s) != 0 && r.eval(s) != 0 ? 1 : 0;
            }
            return left;
        }

        Expr comparison() {
            Expr l = sum();
            for (String op : new String[] {"<=", ">=", "==", "!=", "<", ">"}) {
                if (!eat(op)) continue;
                Expr r = sum();
                return switch (op) {
                    case "<=" -> s -> l.eval(s) <= r.eval(s) ? 1 : 0;
                    case ">=" -> s -> l.eval(s) >= r.eval(s) ? 1 : 0;
                    case "==" -> s -> l.eval(s) == r.eval(s) ? 1 : 0;
                    case "!=" -> s -> l.eval(s) != r.eval(s) ? 1 : 0;
                    case "<" -> s -> l.eval(s) < r.eval(s) ? 1 : 0;
                    default -> s -> l.eval(s) > r.eval(s) ? 1 : 0;
                };
            }
            return l;
        }

        Expr sum() {
            Expr left = product();
            while (true) {
                Expr l = left;
                if (eat("+")) {
                    Expr r = product();
                    left = s -> l.eval(s) + r.eval(s);
                } else if (eat("-")) {
                    Expr r = product();
                    left = s -> l.eval(s) - r.eval(s);
                } else return left;
            }
        }

        Expr product() {
            Expr left = unary();
            while (true) {
                Expr l = left;
                if (eat("*")) {
                    Expr r = unary();
                    left = s -> l.eval(s) * r.eval(s);
                } else if (eat("/")) {
                    Expr r = unary();
                    left = s -> l.eval(s) / r.eval(s);
                } else return left;
            }
        }

        Expr unary() {
            if (eat("-")) {
                Expr e = unary();
                return s -> -e.eval(s);
            }
            if (eat("!")) {
                Expr e = unary();
                return s -> e.eval(s) == 0 ? 1 : 0;
            }
            return primary();
        }

        Expr primary() {
            skipSpace();
            if (eat("(")) {
                Expr e = ternary();
                if (!eat(")")) throw error("expected ')'");
                return e;
            }
            if (pos >= src.length()) throw error("unexpected end");
            char c = src.charAt(pos);
            if (Character.isDigit(c) || c == '.') return number();
            if (Character.isLetter(c) || c == '_') return name();
            throw error("unexpected '" + c + "'");
        }

        Expr number() {
            int start = pos;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) pos++;
            if (pos < src.length() && src.charAt(pos) == 'f') pos++; // Bedrock allows 1.0f
            double value = Double.parseDouble(src.substring(start, pos).replace("f", ""));
            return constant(value);
        }

        Expr name() {
            int start = pos;
            while (pos < src.length() && (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_' || src.charAt(pos) == '.')) pos++;
            String name = src.substring(start, pos);
            int dot = name.indexOf('.');
            if (dot < 0) throw error("unknown name " + name);
            String space = name.substring(0, dot), member = name.substring(dot + 1);
            return switch (space) {
                case "math" -> math(member);
                case "variable", "v", "temp", "t" -> {
                    String key = "variable." + member;
                    yield s -> s.get(key);
                }
                case "query", "q" -> {
                    String key = "query." + member;
                    yield s -> s.get(key);
                }
                default -> throw error("unknown name " + name);
            };
        }

        Expr math(String member) {
            if (member.equals("pi")) return constant(Math.PI);
            List<Expr> args = new ArrayList<>();
            if (!eat("(")) throw error("expected '(' after math." + member);
            if (!eat(")")) {
                do args.add(ternary()); while (eat(","));
                if (!eat(")")) throw error("expected ')'");
            }
            if (member.equals("random") || member.equals("random_integer")) {
                if (args.size() != 2) throw error("math." + member + " takes 2 arguments");
                Expr lo = args.get(0), hi = args.get(1);
                return member.equals("random")
                        ? s -> { double a = lo.eval(s); return a + (hi.eval(s) - a) * s.random(); }
                        : s -> { double a = Math.round(lo.eval(s)); return a + Math.floor((Math.round(hi.eval(s)) - a + 1) * s.random()); };
            }
            Fn fn = FUNCTIONS.get(member);
            if (fn == null) throw error("unsupported math." + member);
            if (args.size() != fn.arity()) throw error("math." + member + " takes " + fn.arity() + " arguments");
            Expr[] a = args.toArray(Expr[]::new);
            return s -> {
                double[] values = new double[a.length];
                for (int i = 0; i < a.length; i++) values[i] = a[i].eval(s);
                return fn.body().apply(values);
            };
        }
    }
}
