package com.navesdev.grimoireknights.core.vfx.molang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class MolangTest {
    private static Molang.Scope scope(Map<String, Double> vars, double random) {
        return new Molang.Scope() {
            @Override
            public double get(String name) {
                return vars.getOrDefault(name, 0.0);
            }

            @Override
            public double random() {
                return random;
            }
        };
    }

    private static double eval(String source) {
        return eval(source, Map.of());
    }

    private static double eval(String source, Map<String, Double> vars) {
        return Molang.parse(source).eval(scope(vars, 0.5));
    }

    @Test
    void arithmeticFollowsPrecedence() {
        assertEquals(7, eval("1 + 2 * 3"), 1e-9);
        assertEquals(9, eval("(1 + 2) * 3"), 1e-9);
        assertEquals(-4, eval("-2 * 2"), 1e-9);
        assertEquals(2.5, eval("10 / 4"), 1e-9);
        assertEquals(1, eval("5 - 2 - 2"), 1e-9);
    }

    @Test
    void comparisonsAndTernaryPickABranch() {
        assertEquals(2.5, eval("variable.radius > 0 ? variable.radius : 2.5"), 1e-9);
        assertEquals(4, eval("variable.radius > 0 ? variable.radius : 2.5", Map.of("variable.radius", 4.0)), 1e-9);
        assertEquals(1, eval("1 <= 1 && !(2 == 3) || 0"), 1e-9);
    }

    @Test
    void variablesReadTheScopeWithShortPrefixesAndAnyCase() {
        Map<String, Double> vars = Map.of("variable.particle_age", 0.25, "variable.particle_lifetime", 0.5);
        assertEquals(0.5, eval("variable.particle_age / variable.particle_lifetime", vars), 1e-9);
        assertEquals(0.5, eval("v.particle_age / Variable.Particle_Lifetime", vars), 1e-9);
        assertEquals(0, eval("variable.unset"), 1e-9);
    }

    @Test
    void mathFunctionsUseDegrees() {
        assertEquals(1, eval("math.sin(90)"), 1e-9);
        assertEquals(1.5, eval("math.lerp(1, 2, 0.5)"), 1e-9);
        assertEquals(8, eval("math.pow(2, 3)"), 1e-9);
        assertEquals(2, eval("math.clamp(5, 0, 2)"), 1e-9);
        assertEquals(3, eval("math.max(1, math.abs(-3))"), 1e-9);
        assertEquals(Math.PI, eval("math.pi"), 1e-9);
    }

    @Test
    void randomComesFromTheScope() {
        assertEquals(6, eval("math.random(4, 8)"), 1e-9);
        assertEquals(6, eval("math.random_integer(4, 8)"), 1e-9);
    }

    @Test
    void numbersAndPlainValuesParse() {
        assertEquals(0.35, eval("0.35"), 1e-9);
        assertEquals(1, Molang.constant(1).eval(scope(Map.of(), 0)), 1e-9);
    }

    @Test
    void unsupportedSyntaxFailsAtParse() {
        assertThrows(IllegalArgumentException.class, () -> Molang.parse("math.nope(1)"));
        assertThrows(IllegalArgumentException.class, () -> Molang.parse("1 +"));
        assertThrows(IllegalArgumentException.class, () -> Molang.parse("variable.a = 1;"));
        assertThrows(IllegalArgumentException.class, () -> Molang.parse("math.lerp(1, 2)"));
    }
}
