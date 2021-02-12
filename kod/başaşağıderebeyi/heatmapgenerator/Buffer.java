/**
 * başaşağıderebeyi.heatmapgenerator.Buffer.java
 * sürüm / 12 Şub 2021 / 20:10:09
 * Cem GEÇGEL (BaşAşağıDerebeyi)
 */
package başaşağıderebeyi.heatmapgenerator;

public class Buffer {
	public byte[] data;
	public int pointer;
	
	public byte readByte() {
		return data[pointer++];
	}
	
	public int readInt() {
		int a = 0;
		for (int i = 0; i < 4; i++)
			a |= readByte() << (3 - i) * 8;
		return a;
	}
	
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}
	
	public Buffer writeByte(byte a) {
		data[pointer++] = a;
		return this;
	}
	
	public Buffer writeInt(int a) {
		return 
				writeByte((byte)(a >> 24)).
				writeByte((byte)(a >> 16)).
				writeByte((byte)(a >> 8)).
				writeByte((byte)(a >> 0));
	}
	
	public Buffer writeFloat(float a) {
		return writeInt(Float.floatToIntBits(a));
	}
}
