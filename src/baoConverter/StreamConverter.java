package baoConverter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StreamConverter {

//---------------------------------------------------

	public static void buffer_write_string(DataOutputStream out, String text) throws IOException {
		byte[] bytes = text.getBytes();
		for (int i = 0; i < text.length(); i++) {
			buffer_write_u8(out, bytes[i]);
		}
	}

	public static void buffer_write_u32(DataOutputStream out, int i) throws IOException {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		b.putInt(i);

		out.write(b.array());

	}

	public static void buffer_write_u16(DataOutputStream out, int i) throws IOException {
		out.write((byte) i);
		out.write((byte) (i >> 8));

	}

	public static void buffer_write_s16(DataOutputStream out, int i) throws IOException {
		out.write((byte) i);
		out.write((byte) (i >> 8));

	}

	public static void buffer_write_s32(DataOutputStream out, int i) throws IOException {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		b.putInt(i);

		out.write(b.array());
	}

	public static void buffer_write_u8(DataOutputStream out, int i) throws IOException {
		out.writeByte((byte) i);
	}

	public static void buffer_write_s8(DataOutputStream out, int i) throws IOException {
		out.writeByte((byte) i);
	}

	public static void buffer_write_f64(DataOutputStream out, double i) throws IOException {
		ByteBuffer b = ByteBuffer.allocate(8);
		b.order(ByteOrder.LITTLE_ENDIAN);
		b.putDouble(i);

		out.write(b.array());

	}

	public static void buffer_write_f32(DataOutputStream out, float i) throws IOException {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		b.putFloat(i);

		out.write(b.array());

	}

}