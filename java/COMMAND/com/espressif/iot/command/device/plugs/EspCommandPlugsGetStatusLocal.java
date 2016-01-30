package com.espressif.iot.command.device.plugs;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.ui.device.Fx2nControl;

public class EspCommandPlugsGetStatusLocal implements
		IEspCommandPlugsGetStatusLocal {
	@Override
	public String getLocalUrl(InetAddress inetAddress) {
		// return "http://" + inetAddress.getHostAddress() + "/"
		// + "config?command=switchs";
		return "http://" + inetAddress.getHostAddress() + "/"
				+ "config?command=fx2n";
	}

	private IEspStatusPlugs parsePlugsResponse(JSONObject resultJSON) {

		IEspStatusPlugs plugsStatus = new EspStatusPlugs();
		try {
			List<IAperture> apertures = new ArrayList<IAperture>();
			JSONObject statusJSON = resultJSON.getJSONObject(KEY_PLUGS_STATUS);
			int count = statusJSON.getInt(KEY_APERTURE_COUNT);
			int valueSum = statusJSON.getInt(KEY_PLUGS_VALUE);
			String bitValues = "";
			boolean isOn;
			try {
				bitValues = statusJSON.getString(KEY_PLUGS_BIT_VALUE);
			} catch (JSONException e) {
			}
			for (int i = 0; i < count; i++) {
				IAperture aperture = new EspPlugsAperture(i);
				aperture.setTitle("Plug " + i);
				if (bitValues != "" && bitValues.length() > i) {
					isOn = bitValues.getBytes()[i] != '0';
				} else {
					isOn = (valueSum >> i) % 2 == 1;
				}
				aperture.setOn(isOn);

				apertures.add(aperture);
			}

			plugsStatus.setStatusApertureList(apertures);
			return plugsStatus;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return plugsStatus;
	}

	private IEspStatusPlugs parseControlResponse(JSONObject resultJSON) {
		IEspStatusPlugs plugsStatus = new EspStatusPlugs();
		try {
			int result = resultJSON.getInt("result");
			if (result > 0) {
				String value = "";
				if (resultJSON.has("value"))
					value = resultJSON.getString("value");
				plugsStatus.setValue(value);
			}
			plugsStatus.setResult(result);
			Fx2nControl.setLastStatus(plugsStatus);
			return plugsStatus;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return plugsStatus;
	}

	@Override
	public IEspStatusPlugs doCommandPlugsGetStatusLocal(
			InetAddress inetAddress, String deviceBssid, boolean isMeshDevice) {
		try {
			String url = getLocalUrl(inetAddress);
			JSONObject resultJSON = null;
			if (deviceBssid == null || !isMeshDevice) {
				resultJSON = EspBaseApiUtil.Get(url);
			} else {
				resultJSON = EspBaseApiUtil.GetForJson(url, deviceBssid);
			}

			if (resultJSON == null) {
				return null;
			}

			// return parsePlugsResponse(resultJSON);
			return parseControlResponse(resultJSON);
		} catch (Exception e) {
		}
		return null;
	}
}
