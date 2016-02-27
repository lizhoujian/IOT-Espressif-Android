package com.espressif.iot.ui.device;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.os.Handler;

import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public final class Fx2nControl {
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
	public final static int CMD_WRITE = 1;

	public final static int REQUEST_CONTROL = 10;
	public final static int REQUEST_LAN_IP = 11;
	public final static int REQUEST_SERIAL_SWITCH = 12;
	public final static int REQUEST_PLC_RUN_STOP = 13;
	public final static int REQUEST_PLC_REGISTER_COUNT = 14;
	public final static String[] addrTypeitems = { "X", "Y", "S", "T", "C",
			"D", "D*", "M", "M*", "TV16", "CV16", "CV32" };
	public final static int[] addrTypeValues = { Fx2nControl.REG_X,
			Fx2nControl.REG_Y, Fx2nControl.REG_S, Fx2nControl.REG_T,
			Fx2nControl.REG_C, Fx2nControl.REG_D, Fx2nControl.REG_DS,
			Fx2nControl.REG_M, Fx2nControl.REG_MS, Fx2nControl.REG_TV16,
			Fx2nControl.REG_CV16, Fx2nControl.REG_CV32 };
	public final static int[] addrTypeValueLen = { 1, 1, 1, 1, 1, 2, 2, 1, 1,
			2, 2, 4 };

	public static IEspStatusPlugs lastStatus = null;

	public static Handler mHandler = null;

	public final static void setHandler(Handler handler) {
		Fx2nControl.mHandler = handler;
	}

	public final static void setLastStatus(IEspStatusPlugs status) {
		lastStatus = status;
		Handler handler = null;
		handler = status.getHandler();
		if (handler == null && mHandler != null) {
			handler = mHandler;
		}
		if (handler != null) {
			String action = status.getAction();
			if (action != null) {
				if (action.equalsIgnoreCase("control")
						&& status.getCmd() == CMD_READ
						&& status.getResult() > 0) {
					Map<String, Object> tag = status.getTag();
					if (tag == null) {
						tag = new HashMap<String, Object>();
					}
					tag.put("value", status.getValue());
					handler.sendMessage(handler.obtainMessage(
							Fx2nControl.REQUEST_CONTROL, tag));
				} else if (action.equalsIgnoreCase("lan_ip")) {
					handler.sendMessage(handler.obtainMessage(
							Fx2nControl.REQUEST_LAN_IP, status.getValue()));
				} else if (action.equalsIgnoreCase("serial_switch_set")
						|| action.equalsIgnoreCase("serial_switch_get")) {
					handler.sendMessage(handler.obtainMessage(
							Fx2nControl.REQUEST_SERIAL_SWITCH,
							status.getResult(), 0));
				} else if (action.equalsIgnoreCase("plc_run_stop_set")
						|| action.equalsIgnoreCase("plc_run_stop_get")) {
					handler.sendMessage(handler.obtainMessage(
							Fx2nControl.REQUEST_PLC_RUN_STOP,
							status.getResult(), 0));
				} else if (action.equalsIgnoreCase("reg_bits")) {
					handler.sendMessage(handler.obtainMessage(
							Fx2nControl.REQUEST_PLC_REGISTER_COUNT,
							status.getValue()));
				}
			}
		}
	}

	public final static IEspStatusPlugs getLastStatus() {
		return lastStatus;
	}

	public final static byte[] int2Bytes(int value, int len) {
		byte[] b = new byte[len];
		for (int i = 0; i < len; i++) {
			b[i] = (byte) ((value >> 8 * i) & 0xff);
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

	public final static int hexStringToInt(String hexString) {
		byte[] bytes = hexStringToBytes(hexString);
		int i;
		int ret = 0;
		if (bytes != null) {
			for (i = 0; i < bytes.length; i++) {
				ret |= ((bytes[i] & 0xff) << i * 8);
			}
		}
		return ret;
	}

	public final static int[] hexStringToInt(String hexString, int unitLen) {
		byte[] bytes = hexStringToBytes(hexString);
		int i, j, len, v;
		int[] ret = null;
		if (bytes != null) {
			len = bytes.length / unitLen;
			ret = new int[len];
			for (i = 0; i < len; i++) {
				for (j = 0; j < unitLen; j++) {
					ret[i] |= ((bytes[i * unitLen + j] & 0xff) << j * 8);
				}
			}
		}
		return ret;
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	//
	// public final static byte[] hexStringToBytes(String hexString) {
	// if (hexString == null || hexString.equals("")) {
	// return null;
	// }
	// int length = hexString.length() / 2;
	// byte[] d = new byte[length];
	// for (int i = 0; i < length; i++) {
	// int pos = i * 2;
	// d[i] = Byte.decode("0x" + hexString.substring(pos, pos + 2));
	// }
	// return d;
	// }

	public final static boolean[] bytesToBits(byte[] bytes) {
		boolean[] retValues = new boolean[bytes.length * 8];
		int i, j, tmp;
		for (i = 0; i < bytes.length; i++) {
			for (j = 0; j < 8; j++) {
				tmp = (bytes[i] >> j) & 0x1;
				if (tmp > 0) {
					retValues[i * 8 + j] = true;
				} else {
					retValues[i * 8 + j] = false;
				}
			}
		}
		return retValues;
	}
}
