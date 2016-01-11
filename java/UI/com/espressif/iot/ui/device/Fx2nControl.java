package com.espressif.iot.ui.device;

public class Fx2nControl {
	public final static int REG_D = 0;
	public final static int REG_M = 1;
	public final static int REG_T = 2;
	public final static int REG_S = 3;
	public final static int REG_C = 4;
	public final static int REG_X = 5;
	public final static int REG_Y = 6;
	public final static int REG_MS = 7;
	public final static int REG_YP = 8;
	public final static int REG_TO = 9;
	public final static int REG_MP = 10;
	public final static int REG_CO = 11;
	public final static int REG_TR = 12;
	public final static int REG_CR = 13;
	public final static int REG_TV16 = 14;
	public final static int REG_CV16 = 15;
	public final static int REG_CV32 = 16;
	public final static int REG_DS = 17;

	public final static int CMD_FORCE_ON = 7;
	public final static int CMD_FORCE_OFF = 8;
	public final static int CMD_READ = 0;
	public final static int CMD_WRITE = 0;

	public static byte[] int2Bytes(int value, int len) {
		byte[] b = new byte[len];
		for (int i = 0; i < len; i++) {
			b[len - i - 1] = (byte) ((value >> 8 * i) & 0xff);
		}
		return b;
	}

	public final static String toHexString(int v, int len) {
		int i;
		String tmp;
		String ret = "";
		byte[] bytes = int2Bytes(v, len);
		for (i = 0; i < len; i++) {
			tmp = Integer.toHexString(bytes[i] & 0xFF);
			if (tmp.length() == 1) {
				tmp = "0" + tmp;
			}
			ret += tmp;
		}
		return ret;
	}
}
