package com.espressif.iot.command.device.plugs;

import java.net.InetAddress;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.ui.device.Fx2nControl;

public class EspCommandPlugsPostStatusLocal implements
		IEspCommandPlugsPostStatusLocal {
	@Override
	public String getLocalUrl(InetAddress inetAddress) {
		// return "http://" + inetAddress.getHostAddress() + "/"
		// + "config?command=switchs";
		return "http://" + inetAddress.getHostAddress() + "/"
				+ "config?command=fx2n";
	}

	private JSONObject createControlRequest(IEspStatusPlugs status) {
		JSONObject params = new JSONObject();
		try {
			params.put("action", status.getAction());
			params.put("cmd", status.getCmd());
			params.put("addr_type", status.getAddrType());
			params.put("addr", status.getAddr());
			params.put("len", status.getLen());
			if (status.getValue() != null && status.getValue().trim() != "") {
				params.put("value", status.getValue());
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return params;
	}

	private JSONObject createPlugsRequest(IEspStatusPlugs status) {
		JSONObject params = new JSONObject();
		JSONObject statusJSON = new JSONObject();
		try {
			List<IAperture> apertures = status.getStatusApertureList();
			int valueSum = 0;
			String bit_values = "";
			for (IAperture aperture : apertures) {
				int value;
				if (aperture.isOn()) {
					value = 1 << aperture.getId();
					bit_values += "1";
				} else {
					value = 0;
					bit_values += "0";
				}
				valueSum += value;
			}
			statusJSON.put(KEY_PLUGS_VALUE, valueSum);
			statusJSON.put(KEY_APERTURE_COUNT, apertures.size());
			statusJSON.put(KEY_PLUGS_BIT_VALUE, bit_values);
			params.put(KEY_PLUGS_STATUS, statusJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return params;
	}

	private void parseControlResponse(IEspStatusPlugs oldstatus,
			JSONObject resultJSON) {
		EspStatusPlugs status = new EspStatusPlugs();
		try {
			int result = resultJSON.getInt("result");
			if (result > 0) {
				String value = "";
				if (resultJSON.has("value"))
					value = resultJSON.getString("value");
				status.setValue(value);
			}
			status.setCmd(oldstatus.getCmd());
			status.setAddrType(oldstatus.getAddrType());
			status.setResult(result);
			status.setAction(oldstatus.getAction());
			status.setHandler(oldstatus.getHandler());
			Fx2nControl.setLastStatus(status);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean doCommandPlugsPostStatusLocal(InetAddress inetAddress,
			IEspStatusPlugs status, String deviceBssid, boolean isMeshDevice) {
		try {
			String url = getLocalUrl(inetAddress);

			JSONObject params = createControlRequest(status);

			JSONObject result;
			if (deviceBssid == null || !isMeshDevice) {
				result = EspBaseApiUtil.Post(url, params);
			} else {
				result = EspBaseApiUtil.PostForJson(url, deviceBssid, params);
			}
			parseControlResponse(status, result);
			return result != null;
		} catch (Exception e) {
		}
		return false;
	}
}
