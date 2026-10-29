"""Minimal NBT reader/writer for structure templates (gzip, big endian)."""
import gzip
import struct

END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY = range(13)


class Tag:
    """Typed value for when the Python type is ambiguous (byte, short, float, long...)."""

    def __init__(self, kind, value):
        self.kind = kind
        self.value = value

    def __repr__(self):
        return f"Tag({self.kind}, {self.value!r})"


def byte(v): return Tag(BYTE, v)
def short(v): return Tag(SHORT, v)
def long(v): return Tag(LONG, v)
def float_(v): return Tag(FLOAT, v)
def double(v): return Tag(DOUBLE, v)


def _kind(v):
    if isinstance(v, Tag): return v.kind
    if isinstance(v, bool): return BYTE
    if isinstance(v, int): return INT
    if isinstance(v, float): return DOUBLE
    if isinstance(v, str): return STRING
    if isinstance(v, dict): return COMPOUND
    if isinstance(v, list): return LIST
    raise TypeError(type(v))


def _write(out, kind, v):
    if isinstance(v, Tag): v = v.value
    if kind == BYTE: out += struct.pack('>b', int(v))
    elif kind == SHORT: out += struct.pack('>h', v)
    elif kind == INT: out += struct.pack('>i', v)
    elif kind == LONG: out += struct.pack('>q', v)
    elif kind == FLOAT: out += struct.pack('>f', v)
    elif kind == DOUBLE: out += struct.pack('>d', v)
    elif kind == STRING:
        b = v.encode('utf-8')
        out += struct.pack('>H', len(b)) + b
    elif kind == LIST:
        inner = _kind(v[0]) if v else END
        out += struct.pack('>bi', inner, len(v))
        for item in v: _write(out, inner, item)
    elif kind == COMPOUND:
        for name, item in v.items():
            k = _kind(item)
            out += struct.pack('>b', k)
            _write(out, STRING, name)
            _write(out, k, item)
        out += b'\x00'
    elif kind == INT_ARRAY:
        out += struct.pack('>i', len(v)) + struct.pack(f'>{len(v)}i', *v)
    else:
        raise ValueError(kind)


def write(path, root):
    out = bytearray()
    out += struct.pack('>b', COMPOUND)
    _write(out, STRING, '')
    _write(out, COMPOUND, root)
    # mtime=0 keeps the output deterministic
    with open(path, 'wb') as f:
        f.write(gzip.compress(bytes(out), mtime=0))


def read(path):
    data = gzip.decompress(open(path, 'rb').read())
    pos = [0]

    def take(fmt):
        size = struct.calcsize(fmt)
        v = struct.unpack_from(fmt, data, pos[0])
        pos[0] += size
        return v[0] if len(v) == 1 else v

    def payload(kind):
        if kind == BYTE: return take('>b')
        if kind == SHORT: return take('>h')
        if kind == INT: return take('>i')
        if kind == LONG: return take('>q')
        if kind == FLOAT: return take('>f')
        if kind == DOUBLE: return take('>d')
        if kind == BYTE_ARRAY: return [take('>b') for _ in range(take('>i'))]
        if kind == STRING:
            n = take('>H')
            s = data[pos[0]:pos[0] + n].decode('utf-8')
            pos[0] += n
            return s
        if kind == LIST:
            inner, n = take('>bi')
            return [payload(inner) for _ in range(n)]
        if kind == COMPOUND:
            result = {}
            while True:
                k = take('>b')
                if k == END: return result
                name = payload(STRING)
                result[name] = payload(k)
        if kind == INT_ARRAY: return [take('>i') for _ in range(take('>i'))]
        if kind == LONG_ARRAY: return [take('>q') for _ in range(take('>i'))]
        raise ValueError(kind)

    take('>b')
    payload(STRING)
    return payload(COMPOUND)
