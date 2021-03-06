package com.espressif.iot.action.device.common;

import java.net.InetAddress;
import java.util.List;

import com.espressif.iot.command.device.light.EspCommandLightGetEspnowLocal;
import com.espressif.iot.command.device.light.EspCommandLightGetStatusLocal;
import com.espressif.iot.command.device.light.IEspCommandLightGetEspnowLocal;
import com.espressif.iot.command.device.light.IEspCommandLightGetStatusLocal;
import com.espressif.iot.command.device.plug.EspCommandPlugGetStatusLocal;
import com.espressif.iot.command.device.plug.IEspCommandPlugGetStatusLocal;
import com.espressif.iot.command.device.plugs.EspCommandPlugsGetStatusLocal;
import com.espressif.iot.command.device.plugs.IEspCommandPlugsGetStatusLocal;
import com.espressif.iot.command.device.remote.EspCommandRemoteGetStatusLocal;
import com.espressif.iot.command.device.remote.IEspCommandRemoteGetStatusLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.device.IEspDeviceRemote;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.IEspStatusEspnow;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public class EspActionDeviceGetStatusLocal implements IEspActionDeviceGetStatusLocal
{
    
    @Override
    public boolean doActionDeviceGetStatusLocal(IEspDevice device)
    {
        EspDeviceType deviceType = device.getDeviceType();
        switch (deviceType)
        {
            case FLAMMABLE:
                break;
            case HUMITURE:
                break;
            case VOLTAGE:
                break;
            case LIGHT:
                return executeGetLightStatusLocal(device);
            case PLUG:
                return executeGetPlugStatusLocal(device);
            case REMOTE:
                return executeGetRemoteStatusLocal(device);
            case PLUGS:
                return executeGetPlugsStatusLocal(device);
            case NEW:
                break;
            case ROOT:
                return false;
        }
        throw new IllegalArgumentException();
    }
    
    private boolean executeGetLightStatusLocal(IEspDevice device)
    {
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = false;
        
        IEspStatusLight status;
        if (device instanceof IEspDeviceSSS)
        {
            status = (IEspStatusLight)((IEspDeviceSSS)device).getDeviceStatus();
        }
        else
        {
            status = ((IEspDeviceLight)device).getStatusLight();
        }
        
        // Get rgb period white value
        IEspCommandLightGetStatusLocal lightCommand = new EspCommandLightGetStatusLocal();
        IEspStatusLight lightStatus =
            lightCommand.doCommandLightGetStatusLocal(inetAddress, deviceBssid, isMeshDevice);
        if (lightStatus != null)
        {
            result = true;
            status.setPeriod(lightStatus.getPeriod());
            status.setRed(lightStatus.getRed());
            status.setGreen(lightStatus.getGreen());
            status.setBlue(lightStatus.getBlue());
            status.setCWhite(lightStatus.getCWhite());
            status.setWWhite(lightStatus.getWWhite());
        }
        
        // Get battery value
        boolean batterySuc = false;
        IEspCommandLightGetEspnowLocal batteryCommand = new EspCommandLightGetEspnowLocal();
        List<IEspStatusEspnow> espnowStatusList =
            batteryCommand.doCommandLightGetEspnowLocal(inetAddress, deviceBssid, isMeshDevice);
        List<IEspStatusEspnow> deviceEspnowStatusList = device.getEspnowStatusList();
        deviceEspnowStatusList.clear();
        if (espnowStatusList != null)
        {
            batterySuc = true;
            deviceEspnowStatusList.addAll(espnowStatusList);
        }
        
        return result && batterySuc;
    }
    
    private boolean executeGetPlugStatusLocal(IEspDevice device)
    {
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = false;
        
        IEspCommandPlugGetStatusLocal plugCommand = new EspCommandPlugGetStatusLocal();
        IEspStatusPlug plugStatus =
            plugCommand.doCommandPlugGetStatusLocal(inetAddress, deviceBssid, isMeshDevice);
        if (plugStatus != null)
        {
            result = true;
            IEspStatusPlug status;
            if (device instanceof IEspDeviceSSS)
            {
                status = (IEspStatusPlug)((IEspDeviceSSS)device).getDeviceStatus();
            }
            else
            {
                status = ((IEspDevicePlug)device).getStatusPlug();
            }
            status.setIsOn(plugStatus.isOn());
        }
        
        return result;
    }
    
    private boolean executeGetRemoteStatusLocal(IEspDevice device)
    {
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = false;
        
        IEspCommandRemoteGetStatusLocal remoteCommand = new EspCommandRemoteGetStatusLocal();
        IEspStatusRemote remoteStatus =
            remoteCommand.doCommandRemoteGetStatusLocal(inetAddress, deviceBssid, isMeshDevice);
        if (remoteStatus != null)
        {
            result = true;
            IEspStatusRemote status;
            if (device instanceof IEspDeviceSSS)
            {
                status = (IEspStatusRemote)((IEspDeviceSSS)device).getDeviceStatus();
            }
            else
            {
                status = ((IEspDeviceRemote)device).getStatusRemote();
            }
            status.setAddress(remoteStatus.getAddress());
            status.setCommand(remoteStatus.getCommand());
            status.setRepeat(remoteStatus.getRepeat());
        }
        
        return result;
    }
    
    private boolean executeGetPlugsStatusLocal(IEspDevice device)
    {
        InetAddress inetAddress = device.getInetAddress();
        String deviceBssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        boolean result = false;
        
        IEspCommandPlugsGetStatusLocal plugsCommand = new EspCommandPlugsGetStatusLocal();
        IEspStatusPlugs plugsStatus =
            plugsCommand.doCommandPlugsGetStatusLocal(inetAddress, deviceBssid, isMeshDevice);
        if (plugsStatus != null)
        {
            result = true;
            if (device instanceof IEspDevicePlugs)
            {
                IEspDevicePlugs plugs = (IEspDevicePlugs)device;
                plugs.setStatusPlugs(plugsStatus);
            }
            else if (device instanceof IEspDeviceSSS)
            {
                IEspStatusPlugs statusPlugs = (IEspStatusPlugs)((IEspDeviceSSS)device).getDeviceStatus();
                statusPlugs.setStatusApertureList(plugsStatus.getStatusApertureList());
            }
        }
        
        return result;
    }
}
