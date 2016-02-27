package com.espressif.iot.command.device.plugs;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.ui.device.Fx2nControl;

public class EspCommandPlugsPostStatusInternet implements
		IEspCommandPlugsPostStatusInternet {

	private JSONObject createPlugsParams(IEspStatusPlugs status) {
		JSONObject params = new JSONObject();
		JSONObject dataJSON = new JSONObject();
		String bitValues = "";
		try {
			List<IAperture> apertures = status.getStatusApertureList();
			int valueSum = 0;
			for (IAperture aperture : apertures) {
				int value;
				if (aperture.isOn()) {
					value = 1 << aperture.getId();
					bitValues += "1";
				} else {
					value = 0;
					bitValues += "0";
				}

				valueSum += value;
			}
			dataJSON.put(X, valueSum);
			dataJSON.put(Y, apertures.size());
			dataJSON.put(Z, bitValues);
			params.put(Datapoint, dataJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return params;
	}

	private JSONObject createControlParams(IEspStatusPlugs status) {
		JSONObject params = new JSONObject();
		JSONObject dataJSON = new JSONObject();
		try {
			dataJSON.put(H, status.getAction());
			dataJSON.put(X, status.getCmd());
			dataJSON.put(Y, status.getAddrType());
			dataJSON.put(Z, status.getAddr());
			dataJSON.put(K, status.getValue());
			dataJSON.put(L, status.getLen());
			params.put(Datapoint, dataJSON);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return params;
	}

	private String createControlParams2(IEspStatusPlugs status) {
		String ret;
		ret = "&action=" + status.getAction();
		ret += "&" + X + "=" + status.getCmd();
		ret += "&" + Y + "=" + status.getAddrType();
		ret += "&" + Z + "=" + status.getAddr();
		ret += "&" + L + "=" + status.getLen();
		ret += "&" + K + "=" + status.getValue();
		return ret;
	}

	private void setControlResponse(IEspStatusPlugs oldstatus, JSONObject result) {
		EspStatusPlugs status = new EspStatusPlugs();
		JSONObject dataJSON;
		try {
			dataJSON = result.getJSONObject(Datapoint);
			int x = dataJSON.getInt(X);
			int y = dataJSON.getInt(Y);
			String z = "";
			if (dataJSON.has(Z)) {
				z = dataJSON.getString(Z);
			}
			status.setResult(y);
			status.setValue(z);
			status.setAction(oldstatus.getAction());
			status.setCmd(oldstatus.getCmd());
			status.setAddrType(oldstatus.getAddrType());
			status.setHandler(oldstatus.getHandler());
			status.setTag(oldstatus.getTag());
			Fx2nControl.setLastStatus(status);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean controlPost(String deviceKey, IEspStatusPlugs status) {
		String headerKey = Authorization;
		String headerValue = Token + " " + deviceKey;
		HeaderPair header = new HeaderPair(headerKey, headerValue);

		String params = createControlParams2(status);

		String url = URL_RPC;
		JSONObject result = EspBaseApiUtil.Get(url + params, header);
		if (result == null) {
			return false;
		}

		try {
			int httpStatus = result.getInt(Status);
			setControlResponse(status, result);
			return httpStatus == HttpStatus.SC_OK;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean plugsPost(String deviceKey, IEspStatusPlugs status) {
		String headerKey = Authorization;
		String headerValue = Token + " " + deviceKey;
		HeaderPair header = new HeaderPair(headerKey, headerValue);

		JSONObject params = createPlugsParams(status);

		String url = URL_RPC;
		JSONObject result = EspBaseApiUtil.Post(url, params, header);
		if (result == null) {
			return false;
		}

		try {
			int httpStatus = result.getInt(Status);
			setControlResponse(status, result);
			return httpStatus == HttpStatus.SC_OK;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean doCommandPlugsPostStatusInternet(String deviceKey,
			IEspStatusPlugs status) {
		try {
			// return plugsPost(deviceKey, status);
			return controlPost(deviceKey, status);
		} catch (Exception e) {
		}
		return false;
	}
}
