// automatically generated by the FlatBuffers compiler, do not modify

package com.thepokecraftmod.rks.animation.tranm;

import com.google.flatbuffers.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class Framed16QuatTrack extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_3_3(); }
  public static Framed16QuatTrack getRootAsFramed16QuatTrack(ByteBuffer _bb) { return getRootAsFramed16QuatTrack(_bb, new Framed16QuatTrack()); }
  public static Framed16QuatTrack getRootAsFramed16QuatTrack(ByteBuffer _bb, Framed16QuatTrack obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Framed16QuatTrack __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int frames(int j) { int o = __offset(4); return o != 0 ? bb.getShort(__vector(o) + j * 2) & 0xFFFF : 0; }
  public int framesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }
  public ShortVector framesVector() { return framesVector(new ShortVector()); }
  public ShortVector framesVector(ShortVector obj) { int o = __offset(4); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer framesAsByteBuffer() { return __vector_as_bytebuffer(4, 2); }
  public ByteBuffer framesInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 2); }
  public Vec3s vec(int j) { return vec(new Vec3s(), j); }
  public Vec3s vec(Vec3s obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o) + j * 6, bb) : null; }
  public int vecLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public Vec3s.Vector vecVector() { return vecVector(new Vec3s.Vector()); }
  public Vec3s.Vector vecVector(Vec3s.Vector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 6, bb) : null; }

  public static int createFramed16QuatTrack(FlatBufferBuilder builder,
      int framesOffset,
      int vecOffset) {
    builder.startTable(2);
    Framed16QuatTrack.addVec(builder, vecOffset);
    Framed16QuatTrack.addFrames(builder, framesOffset);
    return Framed16QuatTrack.endFramed16QuatTrack(builder);
  }

  public static void startFramed16QuatTrack(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addFrames(FlatBufferBuilder builder, int framesOffset) { builder.addOffset(0, framesOffset, 0); }
  public static int createFramesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(2, data.length, 2); for (int i = data.length - 1; i >= 0; i--) builder.addShort((short) data[i]); return builder.endVector(); }
  public static void startFramesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(2, numElems, 2); }
  public static void addVec(FlatBufferBuilder builder, int vecOffset) { builder.addOffset(1, vecOffset, 0); }
  public static void startVecVector(FlatBufferBuilder builder, int numElems) { builder.startVector(6, numElems, 2); }
  public static int endFramed16QuatTrack(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Framed16QuatTrack get(int j) { return get(new Framed16QuatTrack(), j); }
    public Framed16QuatTrack get(Framed16QuatTrack obj, int j) {  return obj.__assign(Table.__indirect(__element(j), bb), bb); }
  }
}

