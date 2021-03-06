package com.espressif.iot.model.user;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.espressif.iot.action.IEspActionUserRegisterPhoneInternet;
import com.espressif.iot.command.device.espbutton.IEspButtonConfigureListener;
import com.espressif.iot.device.cache.IEspDeviceCache.NotifyType;
import com.espressif.iot.action.device.New.EspActionDeviceNewGetInfoLocal;
import com.espressif.iot.action.device.New.IEspActionDeviceNewGetInfoLocal;
import com.espressif.iot.action.device.array.EspActionDeviceArrayPostStatus;
import com.espressif.iot.action.device.array.IEspActionDeviceArrayPostStatus;
import com.espressif.iot.action.device.common.EspActionDeviceActivateSharedInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGenerateShareKeyInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGetStatusInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGetStatusLocal;
import com.espressif.iot.action.device.common.EspActionDevicePostStatusInternet;
import com.espressif.iot.action.device.common.EspActionDevicePostStatusLocal;
import com.espressif.iot.action.device.common.EspActionDeviceSleepRebootLocal;
import com.espressif.iot.action.device.common.EspActionDeviceSynchronizeInterentDiscoverLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceActivateSharedInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGenerateShareKeyInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGetStatusInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGetStatusLocal;
import com.espressif.iot.action.device.common.IEspActionDevicePostStatusInternet;
import com.espressif.iot.action.device.common.IEspActionDevicePostStatusLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceSleepRebootLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceSynchronizeInterentDiscoverLocal;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerDeleteInternet;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerGetInternet;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerPostInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerDeleteInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerGetInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerPostInternet;
import com.espressif.iot.action.device.common.upgrade.EspDeviceCheckCompatibility;
import com.espressif.iot.action.device.common.upgrade.EspDeviceGetUpgradeTypeResult;
import com.espressif.iot.action.device.common.upgrade.IEspDeviceCheckCompatibility;
import com.espressif.iot.action.device.common.upgrade.IEspDeviceGetUpgradeTypeResult;
import com.espressif.iot.action.device.espbutton.EspActionEspButtonActionGet;
import com.espressif.iot.action.device.espbutton.EspActionEspButtonActionSet;
import com.espressif.iot.action.device.espbutton.EspActionEspButtonConfigure;
import com.espressif.iot.action.device.espbutton.IEspActionEspButtonActionGet;
import com.espressif.iot.action.device.espbutton.IEspActionEspButtonActionSet;
import com.espressif.iot.action.device.espbutton.IEspActionEspButtonConfigure;
import com.espressif.iot.action.device.esptouch.EspActionDeviceEsptouch;
import com.espressif.iot.action.device.esptouch.IEspActionDeviceEsptouch;
import com.espressif.iot.action.device.humiture.EspActionHumitureGetStatusListInternetDB;
import com.espressif.iot.action.device.humiture.IEspActionHumitureGetStatusListInternetDB;
import com.espressif.iot.action.group.EspActionGroupDeviceDB;
import com.espressif.iot.action.group.EspActionGroupEditDB;
import com.espressif.iot.action.group.IEspActionGroupDeviceDB;
import com.espressif.iot.action.group.IEspActionGroupEditDB;
import com.espressif.iot.action.user.EspActionFindAccountInternet;
import com.espressif.iot.action.user.EspActionGetSmsCaptchaCodeInternet;
import com.espressif.iot.action.user.EspActionThirdPartyLoginInternet;
import com.espressif.iot.action.user.EspActionUserLoginDB;
import com.espressif.iot.action.user.EspActionUserLoginInternet;
import com.espressif.iot.action.user.EspActionUserLoginPhoneInternet;
import com.espressif.iot.action.user.EspActionUserRegisterInternet;
import com.espressif.iot.action.user.EspActionUserRegisterPhoneInternet;
import com.espressif.iot.action.user.EspActionUserResetPassword;
import com.espressif.iot.action.user.IEspActionFindAccountnternet;
import com.espressif.iot.action.user.IEspActionGetSmsCaptchaCodeInternet;
import com.espressif.iot.action.user.IEspActionThirdPartyLoginInternet;
import com.espressif.iot.action.user.IEspActionUserLoginDB;
import com.espressif.iot.action.user.IEspActionUserLoginInternet;
import com.espressif.iot.action.user.IEspActionUserLoginPhoneInternet;
import com.espressif.iot.action.user.IEspActionUserRegisterInternet;
import com.espressif.iot.action.user.IEspActionUserResetPassword;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.db.EspGroupDBManager;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.db.IOTUserDBManager;
import com.espressif.iot.db.greenrobot.daos.ApDB;
import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.db.greenrobot.daos.GroupDB;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceConfigure;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.device.builder.BEspDeviceNew;
import com.espressif.iot.device.builder.BEspDeviceRoot;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine.Direction;
import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.model.device.cache.EspDeviceCache;
import com.espressif.iot.model.device.cache.EspDeviceCacheHandler;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachine;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachineHandler;
import com.espressif.iot.model.device.statemachine.IEspDeviceStateMachineHandler;
import com.espressif.iot.model.group.EspGroup;
import com.espressif.iot.model.group.EspGroupHandler;
import com.espressif.iot.object.db.IApDB;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.esptouch.EsptouchResult;
import com.espressif.iot.type.device.esptouch.IEsptouchListener;
import com.espressif.iot.type.device.esptouch.IEsptouchResult;
import com.espressif.iot.type.device.other.EspButtonKeySettings;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusHumiture;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceCompatibility;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceTypeResult;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.type.user.EspResetPasswordResult;
import com.espressif.iot.type.user.EspThirdPartyLoginPlat;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspStrings;
import com.espressif.iot.util.RandomUtil;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.TimeUtil;

public class EspUser implements IEspUser
{
    
    private final static Logger log = Logger.getLogger(EspUser.class);
    
    private long mUserId;
    private String mUserKey;
    private String mUserName;
    private String mUserEmail;
    
    private List<IEspDevice> mDeviceList = new ArrayList<IEspDevice>();
    private final List<IEspDeviceSSS> mStaDeviceList = new ArrayList<IEspDeviceSSS>();
    private final List<IEspDeviceSSS> mTempStaDeviceList = new ArrayList<IEspDeviceSSS>();
    private final ReentrantLock mDeviceListsLock = new ReentrantLock();
    
    private volatile IEspActionDeviceEsptouch mActionDeviceEsptouch = null;
    private final Object mEsptouchLock = new Object();
    private volatile boolean mIsEsptouchCancelled = false;
    
    private List<IEspGroup> mGroupList = new ArrayList<IEspGroup>();
    
    @Override
    public String toString()
    {
        return "[id=" + mUserId + ",key=" + mUserKey + ",email=" + mUserEmail + "]";
    }
    
    @Override
    public Void saveUserInfoInDB()
    {
        IOTUserDBManager.getInstance().changeUserInfo(mUserId, mUserEmail, mUserKey, mUserName);
        return null;
    }
    
    @Override
    public void setUserEmail(String userEmail)
    {
        this.mUserEmail = userEmail;
    }
    
    @Override
    public String getUserEmail()
    {
        return this.mUserEmail;
    }
    
    @Override
    public void setUserId(long userId)
    {
        this.mUserId = userId;
    }
    
    @Override
    public long getUserId()
    {
        return this.mUserId;
    }
    
    @Override
    public void setUserKey(String userKey)
    {
        this.mUserKey = userKey;
    }
    
    @Override
    public String getUserKey()
    {
        return this.mUserKey;
    }
    
    @Override
    public void setUserName(String userName)
    {
        mUserName = userName;
    }
    
    @Override
    public String getUserName()
    {
        return mUserName;
    }
    
    @Override
    public boolean isLogin()
    {
        return !TextUtils.isEmpty(mUserKey);
    }
    
    @Override
    public List<IEspDevice> getDeviceList()
    {
        // for the mDeviceList maybe changed after the result return,
        // but we don't like UI layer get dirty device list,
        // so we return the copy list to prevent it
        List<IEspDevice> result = new ArrayList<IEspDevice>();
        lockUserDeviceLists();
        result.addAll(mDeviceList);
        unlockUserDeviceLists();
        return result;
    }
    
    private String mLastConnectedSsid;
    
    @Override
    public void setLastConnectedSsid(String ssid)
    {
        mLastConnectedSsid = ssid;
    }
    
    @Override
    public String getLastConnectedSsid()
    {
        return mLastConnectedSsid;
    }
    
    @Override
    public String getLastSelectedApBssid()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        IApDB apDB = iotApDBManager.getLastSelectedApDB();
        if (apDB == null)
        {
            return null;
        }
        else
        {
            return apDB.getBssid();
        }
    }
    
    @Override
    public String getLastSelectedApPassword()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        IApDB apDB = iotApDBManager.getLastSelectedApDB();
        if (apDB == null)
        {
            return null;
        }
        else
        {
            return apDB.getPassword();
        }
    }
    
    @Override
    public List<String[]> getConfiguredAps()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        List<ApDB> apDBs = iotApDBManager.getAllApDBList();
        List<String[]> result = new ArrayList<String[]>();
        for (ApDB apDB : apDBs)
        {
            String[] ap = new String[3];
            ap[0] = apDB.getBssid();
            ap[1] = apDB.getSsid();
            ap[2] = apDB.getPassword();
            result.add(ap);
        }
        return result;
    }
    
    @Override
    public String getApPassword(String bssid)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        return iotApDBManager.getPassword(bssid);
    }
    
    @Override
    public void saveApInfoInDB(String bssid, String ssid, String password)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        iotApDBManager.insertOrReplace(bssid, ssid, password);
    }
    
    @Override
    public void saveApInfoInDB(String bssid, String ssid, String password, String deviceBssid)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        iotApDBManager.insertOrReplace(bssid, ssid, password, deviceBssid);
    }
    
    @Override
    public IEspDevice getUserDevice(String deviceKey)
    {
        // Check Virtual Root router device
        IEspDevice deviceRoot = BEspDeviceRoot.getBuilder().getLocalRoot();
        if (deviceKey.equals(deviceRoot.getKey()))
        {
            return deviceRoot;
        }
        deviceRoot = BEspDeviceRoot.getBuilder().getInternetRoot();
        if (deviceKey.equals(deviceRoot.getKey()))
        {
            return deviceRoot;
        }
        deviceRoot = BEspDeviceRoot.getBuilder().getVirtualMeshRoot();
        if (deviceKey.equals(deviceRoot.getKey()))
        {
            return deviceRoot;
        }
        
        List<IEspDevice> deviceList = getAllDeviceList();
        for (IEspDevice device : deviceList)
        {
            // if the device is DELETED, ignore it
            if (device.getDeviceState().isStateDeleted())
            {
                continue;
            }
            if (deviceKey.equals(device.getKey()))
            {
                return device;
            }
        }
        
        return null;
    }
    
    @Override
    public List<IEspGroup> getGroupList()
    {
        List<IEspGroup> list = new ArrayList<IEspGroup>();
        synchronized (mGroupList)
        {
            for (IEspGroup group : mGroupList)
            {
                if (!group.isStateDeleted())
                {
                    list.add(group);
                }
            }
        }
        return list;
    }
    
    @Override
    public void loadGroupDB()
    {
        List<IEspDevice> userDevices = getAllDeviceList();
        
        EspGroupDBManager dbManager = EspGroupDBManager.getInstance();
        List<GroupDB> groupDBs = new ArrayList<GroupDB>();
        groupDBs.addAll(dbManager.getUserDBCloudGroup(mUserKey));
        groupDBs.addAll(dbManager.getUserDBLocalGroup(mUserKey));
        List<IEspGroup> groups = new ArrayList<IEspGroup>();
        for (GroupDB groupDB : groupDBs)
        {
            IEspGroup group = new EspGroup();
            group.setId(groupDB.getId());
            group.setName(groupDB.getName());
            group.setState(groupDB.getState());
            List<String> bssids = new ArrayList<String>();
            bssids.addAll(dbManager.getDeviceBssids(groupDB.getLocalDeviceBssids()));
            bssids.addAll(dbManager.getDeviceBssids(groupDB.getCloudDeviceBssids()));
            for (String bssid : bssids)
            {
                for (IEspDevice device : userDevices)
                {
                    if (bssid.equals(device.getBssid()))
                    {
                        group.addDevice(device);
                        break;
                    }
                }
            }
            
            groups.add(group);
        }
        
        synchronized (mGroupList)
        {
            mGroupList.clear();
            mGroupList.addAll(groups);
        }
    }
    
    @Override
    public void doActionConfigure(IEspDevice device, String apSsid, WifiCipherType apWifiCipherType, String apPassword)
    {
        String randomToken = RandomUtil.random40();
        IEspDeviceNew deviceNew = (IEspDeviceNew)device;
        deviceNew.setApPassword(apPassword);
        deviceNew.setApSsid(apSsid);
        deviceNew.setApWifiCipherType(apWifiCipherType);
        deviceNew.setKey(randomToken);
        deviceNew.setUserId(mUserId);
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.CONFIGURE);
    }
    
    @Override
    public boolean doActionPostDeviceStatus(IEspDevice device, IEspDeviceStatus status)
    {
        if (device instanceof IEspDeviceArray)
        {
            return doActionPostDeviceArrayStatus((IEspDeviceArray)device, status);
        }
        
        boolean isLocal = device.getDeviceState().isStateLocal();
        if (isLocal)
        {
            IEspActionDevicePostStatusLocal actionLocal = new EspActionDevicePostStatusLocal();
            return actionLocal.doActionDevicePostStatusLocal(device, status);
        }
        else
        {
            IEspActionDevicePostStatusInternet actionInternet = new EspActionDevicePostStatusInternet();
            return actionInternet.doActionDevicePostStatusInternet(device, status);
        }
    }
    
    private boolean doActionPostDeviceArrayStatus(IEspDeviceArray device, IEspDeviceStatus status)
    {
        IEspActionDeviceArrayPostStatus action = new EspActionDeviceArrayPostStatus();
        action.doActionDeviceArrayPostStatus(device, status);
        return true;
    }
    
    @Override
    public boolean doActionGetDeviceStatus(IEspDevice device)
    {
        boolean isLocal = device.getDeviceState().isStateLocal();
        if (isLocal)
        {
            IEspActionDeviceGetStatusLocal actionLocal = new EspActionDeviceGetStatusLocal();
            return actionLocal.doActionDeviceGetStatusLocal(device);
        }
        else
        {
            IEspActionDeviceGetStatusInternet actionInternet = new EspActionDeviceGetStatusInternet();
            return actionInternet.doActionDeviceGetStatusInternet(device);
        }
    }
    
    @Override
    public void doActionDelete(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.DELETE);
    }
    
    @Override
    public void doActionDelete(final Collection<IEspDevice> devices)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(devices, Direction.DELETE);
    }
    
    @Override
    public void doActionRename(IEspDevice device, String deviceName)
    {
        device.setName(deviceName);
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.RENAME);
    }
    
    @Override
    public void doActionUpgradeLocal(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.UPGRADE_LOCAL);
    }
    
    @Override
    public void doActionUpgradeInternet(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.UPGRADE_INTERNET);
    }
    
    @Override
    public void doActionRefreshDevices()
    {
        IEspActionDeviceSynchronizeInterentDiscoverLocal action = new EspActionDeviceSynchronizeInterentDiscoverLocal();
        action.doActionDeviceSynchronizeInterentDiscoverLocal(mUserKey);
    }
    
    @Override
    public void doActionRefreshStaDevices(boolean isSyn)
    {
        IEspActionDeviceSynchronizeInterentDiscoverLocal action = new EspActionDeviceSynchronizeInterentDiscoverLocal();
        action.doActionDeviceSynchronizeDiscoverLocal(isSyn);
    }
    
    private void __loadUserDeviceList()
    {
        lockUserDeviceLists();
        IOTUserDBManager iotUserDBManager = IOTUserDBManager.getInstance();
        List<DeviceDB> deviceDBList = iotUserDBManager.getUserDeviceList(mUserId);
        mDeviceList.clear();
        // add device into mDeviceList by deviceDBList
        for (DeviceDB deviceDB : deviceDBList)
        {
            IEspDevice device = BEspDevice.getInstance().alloc(deviceDB);
            IEspDeviceState deviceState = device.getDeviceState();
            // if the device state is ACTIVATING, resume activating
            if (EspDeviceState.checkValidWithSpecificStates(deviceState, EspDeviceState.ACTIVATING))
            {
                IEspDeviceNew deviceNew = (IEspDeviceNew)device;
                deviceNew.resume();
            }
            // else LOCAL, INTERNET, UPGRADEING_LOCAL, UPGRADING_INTERNET should be set OFFLINE
            else
            {
                if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.LOCAL)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.LOCAL,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateLocal();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState,
                    EspDeviceState.LOCAL,
                    EspDeviceState.INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.OFFLINE)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.OFFLINE,
                        EspDeviceState.RENAMED))
                {
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.UPGRADING_LOCAL)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.UPGRADING_LOCAL,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateUpgradingLocal();
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.UPGRADING_INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.UPGRADING_INTERNET,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateUpgradingInternet();
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithSpecificStates(deviceState, EspDeviceState.DELETED))
                {
                }
                
                else
                {
                    throw new IllegalStateException("device: " + device);
                }
            }
            mDeviceList.add(device);
        }
        // sort device list
        EspDeviceGenericComparator comparator = new EspDeviceGenericComparator();
        Collections.sort(mDeviceList, comparator);
        
        // do rename action or delete action if necessary
        for (IEspDevice device : mDeviceList)
        {
            IEspDeviceState deviceState = device.getDeviceState();
            if (deviceState.isStateRenamed() && !deviceState.isStateDeleted())
            {
                doActionRename(device, device.getName());
            }
            else if (deviceState.isStateDeleted())
            {
                doActionDelete(device);
            }
        }
        unlockUserDeviceLists();
    }
    
    private class EspDeviceGenericComparator implements Comparator<IEspDevice>
    {
        
        @Override
        public int compare(IEspDevice lhs, IEspDevice rhs)
        {
            String device1Name = lhs.getName();
            String device2Name = rhs.getName();
            /**
             * the order is determined by deviceName firstly
             */
            int result = device1Name.compareTo(device2Name);
            if (result == 0)
            {
                String bssid1 = lhs.getBssid();
                String bssid2 = rhs.getBssid();
                /**
                 * if deviceName is the same, it will determined by the bssid
                 */
                return bssid1.compareTo(bssid2);
            }
            return result;
        }
    }
    
    @Override
    public EspLoginResult doActionUserLoginInternet(String userEmail, String userPassword)
    {
        IEspActionUserLoginInternet action = new EspActionUserLoginInternet();
        EspLoginResult result = action.doActionUserLoginInternet(userEmail, userPassword);
        if (result == EspLoginResult.SUC)
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public EspLoginResult doActionThirdPartyLoginInternet(EspThirdPartyLoginPlat espPlat)
    {
        IEspActionThirdPartyLoginInternet action = new EspActionThirdPartyLoginInternet();
        EspLoginResult result = action.doActionThirdPartyLoginInternet(espPlat);
        if (result == EspLoginResult.SUC)
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public EspLoginResult doActionUserLoginPhone(String phoneNumber, String password)
    {
        IEspActionUserLoginPhoneInternet action = new EspActionUserLoginPhoneInternet();
        EspLoginResult result = action.doActionUserLoginPhone(phoneNumber, password);
        if (result == EspLoginResult.SUC)
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public IEspUser doActionUserLoginDB()
    {
        IEspActionUserLoginDB action = new EspActionUserLoginDB();
        IEspUser result = action.doActionUserLoginDB();
        SharedPreferences shared =
            EspApplication.sharedInstance().getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        if (shared.getBoolean(EspStrings.Key.KEY_AUTO_LOGIN, EspDefaults.AUTO_LOGIN))
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public void doActionUserLogout()
    {
        mUserEmail = null;
        mUserId = 0;
        mUserKey = null;
        mUserName = null;
        mGroupList.clear();
        mDeviceList.clear();
        mStaDeviceList.clear();
        mTempStaDeviceList.clear();
    }
    
    @Override
    public void loadUserDeviceListDB()
    {
        __loadUserDeviceList();
    }
    
    @Override
    public void clearUserDeviceLists()
    {
        mDeviceList.clear();
        mStaDeviceList.clear();
        mTempStaDeviceList.clear();
    }
    
    @Override
    public EspRegisterResult doActionUserRegisterInternet(String userName, String userEmail, String userPassword)
    {
        IEspActionUserRegisterInternet action = new EspActionUserRegisterInternet();
        return action.doActionUserRegisterInternet(userName, userEmail, userPassword);
    }
    
    @Override
    public EspRegisterResult doActionUserRegisterPhone(String phoneNumber, String captchaCode, String userPassword)
    {
        IEspActionUserRegisterPhoneInternet action = new EspActionUserRegisterPhoneInternet();
        return action.doActionUserRegisterPhone(phoneNumber, captchaCode, userPassword);
    }
    
    @Override
    public boolean findAccountUsernameRegistered(String userName)
    {
        IEspActionFindAccountnternet action = new EspActionFindAccountInternet();
        return action.doActionFindUsernametInternet(userName);
    }
    
    @Override
    public boolean findAccountEmailRegistered(String email)
    {
        IEspActionFindAccountnternet action = new EspActionFindAccountInternet();
        return action.doActionFindEmailInternet(email);
    }
    
    @Override
    public EspResetPasswordResult doActionResetPassword(String email)
    {
        IEspActionUserResetPassword action = new EspActionUserResetPassword();
        return action.doActionResetPassword(email);
    }
    
    @Override
    public Void doActionDevicesUpdated(boolean isStateMachine)
    {
        return EspDeviceCacheHandler.getInstance().handleUninterruptible(isStateMachine);
    }
    
    // "espressif_" + MAC address's 6 places
    private boolean isESPMeshDevice(String SSID)
    {
        for (int i = 0; i < MESH_DEVICE_SSID_PREFIX.length; i++)
        {
            if (SSID.startsWith(MESH_DEVICE_SSID_PREFIX[i]))
            {
                return true;
            }
        }
        return false;
    }
    
    // "ESP_" + MAC address's 6 places, ordinary device
    // "espressif_" + MAC address's 6 places, mesh device
    private boolean isESPDevice(String SSID)
    {
        for (int i = 0; i < DEVICE_SSID_PREFIX.length; i++)
        {
            if (SSID.startsWith(DEVICE_SSID_PREFIX[i]))
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<IEspDeviceNew> scanSoftapDeviceList()
    {
        return scanSoftapDeviceList(true);
    }
    
    @Override
    public List<IEspDeviceNew> scanSoftapDeviceList(boolean isFilter)
    {
        List<IEspDeviceNew> softapDeviceList = new ArrayList<IEspDeviceNew>();
        List<ScanResult> scanResultList = EspBaseApiUtil.scan();
        for (ScanResult scanResult : scanResultList)
        {
            // mesh device don't support softap mode, although the softap can be found
            if (isESPDevice(scanResult.SSID) && !isESPMeshDevice(scanResult.SSID))
            {
                String ssid = scanResult.SSID;
                // change the device bssid to sta
                String bssid = BSSIDUtil.restoreStaBSSID(scanResult.BSSID);
                int rssi = scanResult.level;
                WifiCipherType wifiCipherType = WifiCipherType.getWifiCipherType(scanResult);
                int state = EspDeviceState.NEW.getStateValue();
                IEspDeviceNew deviceNew = BEspDeviceNew.getInstance().alloc(ssid, bssid, wifiCipherType, rssi, state);
                deviceNew.getDeviceState().addStateNew();
                softapDeviceList.add(deviceNew);
            }
        }
        
        // filter device configured just now
        // List<IEspDevice> mDeviceList
        if (isFilter)
        {
            String bssidSoftap;
            String bssidDevice;
            long timestampConfigure;
            for (IEspDevice device : getDeviceList())
            {
                for (int i = 0; i < softapDeviceList.size(); i++)
                {
                    bssidSoftap = softapDeviceList.get(i).getBssid();
                    bssidDevice = device.getBssid();
                    if (bssidSoftap.equals(bssidDevice))
                    {
                        timestampConfigure = device.getTimestamp();
                        log.error("timestampConfigure = " + TimeUtil.getDateStr(timestampConfigure, null));
                        if (System.currentTimeMillis() - timestampConfigure < SOFTAP_IGNORE_TIMESTAMP
                            || EspDeviceState.checkValidWithSpecificStates(device.getDeviceState(),
                                EspDeviceState.ACTIVATING))
                        {
                            log.error("device = " + device + " is removed");
                            softapDeviceList.remove(i);
                        }
                        break;
                    }
                }
            }
        }
        
        return softapDeviceList;
    }
    
    @Override
    public List<ScanResult> scanApList(boolean isFilter)
    {
        List<ScanResult> scanResultList = EspBaseApiUtil.scan();
        if (isFilter)
        {
            for (int i = 0; i < scanResultList.size(); ++i)
            {
                String ssid = scanResultList.get(i).SSID;
                if (isESPDevice(ssid))
                {
                    scanResultList.remove(i--);
                }
            }
        }
        return scanResultList;
    }
    
    @Override
    public String doActionGenerateShareKey(String ownerDeviceKey)
    {
        IEspActionDeviceGenerateShareKeyInternet action = new EspActionDeviceGenerateShareKeyInternet();
        return action.doActionDeviceGenerateShareKeyInternet(ownerDeviceKey);
    }
    
    @Override
    public boolean doActionActivateSharedDevice(String sharedDeviceKey)
    {
        IEspActionDeviceActivateSharedInternet action = new EspActionDeviceActivateSharedInternet();
        IEspDevice device = action.doActionDeviceActivateSharedInternet(mUserId, mUserKey, sharedDeviceKey);
        boolean isSuc = device != null;
        if (isSuc)
        {
            EspDeviceCache.getInstance().addSharedDeviceCache(device);
            EspDeviceCache.getInstance().notifyIUser(NotifyType.STATE_MACHINE_UI);
        }
        return isSuc;
    }
    
    @Override
    public boolean doActionGetSmsCaptchaCode(String phoneNumber, String state)
    {
        IEspActionGetSmsCaptchaCodeInternet action = new EspActionGetSmsCaptchaCodeInternet();
        return action.doActionGetSmsCaptchaCode(phoneNumber, state);
    }
    
    @Override
    public EspUpgradeDeviceCompatibility checkDeviceCompatibility(IEspDevice device)
    {
        String version = device.getRom_version();
        IEspDeviceCheckCompatibility action = new EspDeviceCheckCompatibility();
        return action.checkDeviceCompatibility(version);
    }
    
    @Override
    public EspUpgradeDeviceTypeResult getDeviceUpgradeTypeResult(IEspDevice device)
    {
        String romVersion = device.getRom_version();
        String latestRomVersion = device.getLatest_rom_version();
        IEspDeviceGetUpgradeTypeResult action = new EspDeviceGetUpgradeTypeResult();
        return action.getDeviceUpgradeTypeResult(romVersion, latestRomVersion);
    }
    
    @Override
    public boolean doActionDeviceTimerGet(IEspDevice device)
    {
        IEspActionDeviceTimerGetInternet action = new EspActionDeviceTimerGetInternet();
        return action.doActionDeviceTimerGet(device);
    }
    
    @Override
    public boolean doActionDeviceTimerPost(IEspDevice device, JSONObject timerJSON)
    {
        IEspActionDeviceTimerPostInternet action = new EspActionDeviceTimerPostInternet();
        return action.doActionDeviceTimerPostInternet(device, timerJSON);
    }
    
    @Override
    public boolean doActionDeviceTimerDelete(IEspDevice device, long timerId)
    {
        IEspActionDeviceTimerDeleteInternet action = new EspActionDeviceTimerDeleteInternet();
        return action.doActionDeviceTimerDeleteInternet(device, timerId);
    }
    
    @Override
    public IOTAddress doActionDeviceNewConnect(IEspDeviceNew device)
    {
        IEspActionDeviceNewGetInfoLocal action = new EspActionDeviceNewGetInfoLocal();
        return action.doActionNewGetInfoLocal(device);
    }
    
    @Override
    public void doActionDeviceSleepRebootLocal(EspDeviceType type)
    {
        IEspActionDeviceSleepRebootLocal action = new EspActionDeviceSleepRebootLocal();
        action.doActionDeviceSleepRebootLocal(type);
    }
    
    @Override
    public List<IEspStatusHumiture> doActionGetHumitureStatusList(IEspDevice device, long startTimestamp,
        long endTimestamp, long interval)
    {
        IEspActionHumitureGetStatusListInternetDB action = new EspActionHumitureGetStatusListInternetDB();
        long deviceId = device.getId();
        String deviceKey = device.getKey();
        return action.doActionHumitureGetStatusListInternetDB(deviceId,
            deviceKey,
            startTimestamp,
            endTimestamp,
            interval);
    }
    
    @Override
    public List<IEspStatusFlammable> doActionGetFlammableStatusList(IEspDevice device, long startTimestamp,
        long endTimestamp, long interval)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<IEspDeviceSSS> __getOriginStaDeviceList()
    {
        return this.mStaDeviceList;
    }
    
    @Override
    public List<IEspDeviceSSS> getStaDeviceList()
    {
        // for the mStaDeviceList maybe changed after the result return,
        // but we don't like UI layer get dirty device list,
        // so we return the copy list to prevent it
        List<IEspDeviceSSS> result = new ArrayList<IEspDeviceSSS>();
        lockUserDeviceLists();
        result.addAll(mStaDeviceList);
        // add temp sta device if not exist
        for (IEspDeviceSSS tempStaDevice : mTempStaDeviceList)
        {
            __addOrUpdateStaDeivce(result, tempStaDevice);
        }
        unlockUserDeviceLists();
        return result;
    }
    
    @Override
    public List<IEspDevice> __getOriginDeviceList()
    {
        return mDeviceList;
    }

    @Override
    public void __clearTempStaDeviceList()
    {
        log.debug("__clearTempStaDeviceList()");
        lockUserDeviceLists();
        mTempStaDeviceList.clear();
        unlockUserDeviceLists();
    }

    private boolean __updateDevice(List<IEspDevice> deviceList, IEspDeviceSSS newStaDevice)
    {
        log.debug("__updateDevice() deviceList:" + deviceList + ", newStaDevice:" + newStaDevice);
        // boolean isChanged = false;
        boolean isExist = false;
        String newBssid = newStaDevice.getBssid();
        InetAddress newInetAddress = newStaDevice.getInetAddress();
        String newParentBssid = newStaDevice.getParentDeviceBssid();
        String newRootBssid = newStaDevice.getRootDeviceBssid();
        for (IEspDevice device : deviceList)
        {
            String bssid = device.getBssid();
            if (newBssid.equals(bssid))
            {
                isExist = true;
                IEspDeviceState deviceState = device.getDeviceState();
                // add device state local if necessary
                boolean isNecessaryAddStateLocal =
                    !deviceState.isStateLocal() && !deviceState.isStateUpgradingInternet()
                        && (deviceState.isStateOffline() || deviceState.isStateInternet());
                if (isNecessaryAddStateLocal)
                {
                    // isChanged = true;
                    deviceState.addStateLocal();
                    log.debug("__updateDevice() addStateLocal");
                }
                
                InetAddress inetAddress = device.getInetAddress();
                String parentBssid = device.getParentDeviceBssid();
                String rootBssid = device.getRootDeviceBssid();
                // update inetAddress if necessary
                if (!newInetAddress.equals(inetAddress))
                {
                    // isChanged = true;
                    device.setInetAddress(newInetAddress);
                    log.debug("__updateDevice() update inetAddress");
                }
                // update parent bssid if necessary
                if (newParentBssid != null && !newParentBssid.equals(parentBssid) || newParentBssid == null
                    && parentBssid != null)
                {
                    // isChanged = true;
                    device.setParentDeviceBssid(newParentBssid);
                    log.debug("__updateDevice() update parentBssid");
                }
                // update root bssid if necessary
                if (newRootBssid != null && !newRootBssid.equals(rootBssid) || newRootBssid == null
                    && rootBssid != null)
                {
                    // isChanged = true;
                    device.setRootDeviceBssid(newRootBssid);
                    log.debug("__updateDevice() update rootBssid");
                }
                log.debug("__updateDevice() update break");
                break;
            }
        }
        // return isChanged || isExist;
        return isExist;
    }
    
    private boolean __addOrUpdateStaDeivce(List<IEspDeviceSSS> staDeviceList, IEspDeviceSSS newStaDevice)
    {
        log.debug("__addOrUpdateStaDeivce() staDeviceList:" + staDeviceList + ", newStaDevice:" + newStaDevice);
        boolean isChanged = false;
        boolean isExist = false;
        String newBssid = newStaDevice.getBssid();
        InetAddress newInetAddress = newStaDevice.getInetAddress();
        String newParentBssid = newStaDevice.getParentDeviceBssid();
        String newRootBssid = newStaDevice.getRootDeviceBssid();
        for (IEspDeviceSSS staDevice : staDeviceList)
        {
            String bssid = staDevice.getBssid();
            if (newBssid.equals(bssid))
            {
                isExist = true;
                InetAddress inetAddress = staDevice.getInetAddress();
                String parentBssid = staDevice.getParentDeviceBssid();
                String rootBssid = staDevice.getRootDeviceBssid();
                // update inetAddress if necessary
                if (!newInetAddress.equals(inetAddress))
                {
                    isChanged = true;
                    staDevice.setInetAddress(newInetAddress);
                    log.debug("__addOrUpdateStaDeivce() update inetAddress");
                }
                // update parent bssid if necessary
                if (newParentBssid == null && parentBssid != null || newParentBssid != null
                    && !newParentBssid.equals(parentBssid))
                {
                    isChanged = true;
                    staDevice.setParentDeviceBssid(newParentBssid);
                    log.debug("__addOrUpdateStaDeivce() update parentBssid");
                }
                // update root bssid if necessary
                if (newRootBssid == null && rootBssid != null || newRootBssid != null
                    && !newRootBssid.equals(rootBssid))
                {
                    isChanged = true;
                    staDevice.setRootDeviceBssid(newRootBssid);
                    log.debug("__addOrUpdateStaDeivce() update rootBssid");
                }
                log.debug("__addOrUpdateStaDeivce() update break");
                break;
            }
        }
        if (!isExist)
        {
            isChanged = true;
            staDeviceList.add(newStaDevice);
            log.debug("__addOrUpdateStaDeivce() add newStaDevice");
        }
        return isChanged;
    }
    
    @Override
    public void __addTempStaDeviceList(List<IOTAddress> iotAddressList)
    {
        log.debug("__addTempStaDeviceList() iotAddressList:" + iotAddressList);
        lockUserDeviceLists();
        boolean isDeviceChanged = false;
        for (IOTAddress iotAddress : iotAddressList)
        {
            String rootBssid = iotAddress.getRootBssid();
            String bssid = iotAddress.getBSSID();
            String ssid = BSSIDUtil.genDeviceNameByBSSID(bssid);
            iotAddress.setSSID(ssid);
            IEspDeviceSSS tempStaDevice = BEspDevice.createSSSDevice(iotAddress);
            tempStaDevice.setRootDeviceBssid(rootBssid);
            boolean _isTempStaChanged = __addOrUpdateStaDeivce(mTempStaDeviceList, tempStaDevice);
            isDeviceChanged = isDeviceChanged ? true : _isTempStaChanged;
        }
        if (isDeviceChanged)
        {
            log.debug("__addTempStaDeviceList() send local broadcast UI_REFRESH_LOCAL_DEVICE");
            // send ui refresh local devices broadcast when new local devices are found
            Context context = EspApplication.sharedInstance().getApplicationContext();
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            Intent intent = new Intent(EspStrings.Action.UI_REFRESH_LOCAL_DEVICES);
            broadcastManager.sendBroadcast(intent);
        }
        unlockUserDeviceLists();
    }
    
    @Override
    public List<IEspDevice> getAllDeviceList()
    {
        // for the mDeviceList and mStaDeviceList maybe changed after the result return,
        // but we don't like UI layer get dirty device list,
        // so we return the copy list to prevent it
        List<IEspDevice> result = new ArrayList<IEspDevice>();
        lockUserDeviceLists();
        List<IEspDevice> deviceList = new ArrayList<IEspDevice>();
        deviceList.addAll(mDeviceList);
        List<IEspDeviceSSS> staDeviceList = new ArrayList<IEspDeviceSSS>();
        staDeviceList.addAll(mStaDeviceList);
        for (IEspDeviceSSS tempStaDevice : mTempStaDeviceList)
        {
            boolean isExist = __updateDevice(deviceList, tempStaDevice);
            if (!isExist)
            {
                __addOrUpdateStaDeivce(staDeviceList, tempStaDevice);
            }
        }
        result.addAll(deviceList);
        result.addAll(staDeviceList);
        unlockUserDeviceLists();
        return result;
    }
    
    @Override
    public List<IEspDeviceNew> getSoftapDeviceList()
    {
        List<IEspDeviceNew> result = scanSoftapDeviceList(false);
        // it has been locked when call getAllDeviceList(),
        // so thre's no need locking here
        List<IEspDevice> allDeviceList = getAllDeviceList();
        // delete the IEspDeviceNew if the device is belong to getAllDeviceList()
        boolean isExist;
        for (int i = 0; i < result.size(); ++i)
        {
            isExist = false;
            String newDeviceBssid = result.get(i).getBssid();
            for (IEspDevice device : allDeviceList)
            {
                if (device.getBssid().equals(newDeviceBssid))
                {
                    isExist = true;
                    break;
                }
            }
            if (isExist)
            {
                result.remove(i--);
            }
        }
        return result;
    }
    
    @Override
    public void lockUserDeviceLists()
    {
        mDeviceListsLock.lock();
    }
    
    @Override
    public void unlockUserDeviceLists()
    {
        mDeviceListsLock.unlock();
    }
    
    @Override
    public boolean addDeviceSyn(final IEspDeviceSSS device)
    {
        log.debug("addDeviceSyn() device:" + device);
        // start add device task asyn
        boolean isAddDeviceAsynSuc = addDeviceAsyn(device);
        if (isAddDeviceAsynSuc)
        {
            // wait the add device task finished
            IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
            try
            {
                while (!handler.isTaskFinished(device.getBssid()))
                {
                    // busy waiting
                    Thread.sleep(500);
                }
                // let UI refresh the device list first
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                return false;
            }
            // check whether the device is added suc
            return handler.isTaskSuc(device.getBssid());
        }
        else
        {
            return false;
        }
    }
    
    private boolean __ping()
    {
        // send contacting server broadcast when start __ping()
        Context context = EspApplication.sharedInstance().getApplicationContext();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(EspStrings.Action.ESPTOUCH_CONTACTING_SERVER);
        broadcastManager.sendBroadcast(intent);
        
        final String url = "https://iot.espressif.cn/v1/ping/";
        JSONObject result = null;
        for (int retry = 0; result == null && retry < 3; ++retry)
        {
            if (retry != 0)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    return false;
                }
            }
            result = EspBaseApiUtil.Get(url);
        }
        boolean isServerAvailabe = result != null;
        if (isServerAvailabe)
        {
            // send registering devices broadcast when __ping() server suc
            intent = new Intent(EspStrings.Action.ESPTOUCH_REGISTERING_DEVICES);
            broadcastManager.sendBroadcast(intent);
        }
        return isServerAvailabe;
    }
    
    private boolean __addDevicesSyn(final String apSsid, final String apBssid, final String apPassword,
        final boolean isSsidHidden, final boolean requiredActivate, int expectTaskResultCount,
        IEsptouchListener esptouchListener)
    {
        log.debug("addDevicesSyn(apSsid=[" + apSsid + "],apBssid=[" + apBssid + "],apPassword=[" + apPassword
            + "],isSsidHidden=[" + isSsidHidden + "],requiredActivate=[" + requiredActivate + "])");
        
        if (!doEsptouchTaskPrepare())
        {
            log.debug("addDevicesSyn fail for doEsptouchTaskPrepare()");
            return false;
        }
        if (mActionDeviceEsptouch.isExecuted())
        {
            log.debug("addDevicesSyn fail for mActionDeviceEsptouch.isExecuted()");
            return false;
        }
        
        List<IEsptouchResult> esptouchResultList =
            doEsptouchTaskSynAddDeviceAsyn(apSsid,
                apBssid,
                apPassword,
                isSsidHidden,
                requiredActivate,
                expectTaskResultCount,
                esptouchListener);
        boolean isEsptouchSuc = esptouchResultList.get(0).isSuc();
        // when requiredActivate is false, the result is dependent upon isEsptouchSuc
        if (!requiredActivate)
        {
            return isEsptouchSuc;
        }
        
        // ping server
        __ping();
        
        IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
        while (!handler.isAllTasksFinished())
        {
            try
            {
                // busy waiting
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                return false;
            }
        }
        log.error("addDevicesSyn finish");
        // check whether there's device activating suc
        if (isEsptouchSuc)
        {
            for (IEsptouchResult esptouchResult : esptouchResultList)
            {
                String bssid = BSSIDUtil.restoreBSSID(esptouchResult.getBssid());
                // it is suc as long as one device activating suc
                if (handler.isTaskSuc(bssid))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean addDeviceSyn(String apSsid, String apBssid, String apPassword, boolean isSsidHidden,
        boolean requiredActivate)
    {
        return __addDevicesSyn(apSsid, apBssid, apPassword, isSsidHidden, requiredActivate, 1, null);
    }
    

    @Override
    public boolean addDeviceSyn(String apSsid, String apBssid, String apPassword, boolean isSsidHidden,
        boolean requiredActivate, IEsptouchListener esptouchListener)
    {
        return __addDevicesSyn(apSsid, apBssid, apPassword, isSsidHidden, requiredActivate, 1, esptouchListener);
    }
    
    @Override
    public boolean addDevicesSyn(final String apSsid, final String apBssid, final String apPassword,
        final boolean isSsidHidden, final boolean requiredActivate)
    {
        return __addDevicesSyn(apSsid, apBssid, apPassword, isSsidHidden, requiredActivate, 0, null);
    }
    
    @Override
    public boolean addDevicesSyn(String apSsid, String apBssid, String apPassword, boolean isSsidHidden,
        boolean requiredActivate, IEsptouchListener esptouchListener)
    {
        return __addDevicesSyn(apSsid, apBssid, apPassword, isSsidHidden, requiredActivate, 0, esptouchListener);
    }
    
    
    @Override
    public boolean addDeviceAsyn(final IEspDeviceSSS device)
    {
        log.info("addDeviceAsyn() device:" + device);
        IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
        // there's another task about the device is executing, so return false
        if (!handler.isTaskFinished(device.getBssid()))
        {
            return false;
        }
        log.info("addDeviceAsyn() device:" + device + " is finished");
        String randomToken = RandomUtil.random40();
        IEspDeviceConfigure deviceConfigure = device.createConfiguringDevice(randomToken);
        String deviceName = device.getName();
        deviceConfigure.setName(deviceName);
        deviceConfigure.setUserId(mUserId);
        log.info("addDeviceAsyn() device:" + device + " before stateMachine");
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(deviceConfigure, Direction.CONFIGURE);
        log.info("addDeviceAsyn() device:" + device + " after stateMachine");
        return true;
    }
    
    @Override
    public boolean addDevicesAsyn(final String apSsid, final String apBssid, final String apPassword,
        final boolean isSsidHidden, final boolean requiredActivate)
    {
        log.debug("addDevicesAsyn(apSsid=[" + apSsid + "],apBssid=[" + apBssid + "],apPassword=[" + apPassword
            + "],isSsidHidden=[" + isSsidHidden + "],requiredActivate=[" + requiredActivate + "])");
        
        if (!doEsptouchTaskPrepare())
        {
            log.debug("addDevicesAsyn fail for doEsptouchTaskPrepare()");
            return false;
        }
        if (mActionDeviceEsptouch.isExecuted())
        {
            log.debug("addDevicesAsyn fail for mActionDeviceEsptouch.isExecuted()");
            return false;
        }
        
        EspBaseApiUtil.submit(new Runnable()
        {
            @Override
            public void run()
            {
                doEsptouchTaskSynAddDeviceAsyn(apSsid, apBssid, apPassword, isSsidHidden, requiredActivate, 0, null);
            }
        });
        return true;
    }
    
    private List<IEsptouchResult> doEsptouchTaskSynAddDeviceAsyn(final String apSsid, final String apBssid,
        final String apPassword, final boolean isSsidHidden, boolean requiredActivate, int expectTaskResultCount,
        IEsptouchListener esptouchListener)
    {
        log.debug("doEsptouchTaskSynAddDeviceAsyn entrance");
        List<IEsptouchResult> esptouchResultList =
            mActionDeviceEsptouch.doActionDeviceEsptouch(expectTaskResultCount,
                apSsid,
                apBssid,
                apPassword,
                isSsidHidden,
                esptouchListener);
        // when requiredActivate false, we should discover sta devices
        log.debug("doEsptouchTaskSynAddDeviceAsyn requiredActivate = true");
        if (requiredActivate)
        {
            log.debug("doEsptouchTaskSynAddDeviceAsyn add sta device list last discovered");
            if (!mActionDeviceEsptouch.isCancelled() || mActionDeviceEsptouch.isDone())
            {
                // clear the interrupted by esptouchResultList
                log.debug("doEsptouchTaskSynAddDeviceAsyn clear the interrupted set by esptouch");
                Thread.interrupted();
            }
            else
            {
                // for esptouch configured is cancelled, make first result fail
                log.debug("doEsptouchTaskSynAddDeviceAsyn mActionDeviceEsptouch is cancelled");
                esptouchResultList.clear();
                IEsptouchResult failResult = new EsptouchResult(false, null, null);
                esptouchResultList.add(failResult);
                return esptouchResultList;
            }
            
            // add the esptouch devices
            log.debug("doEsptouchTaskSynAddDeviceAsyn add the remainder esptouchResultList");
            for (IEsptouchResult esptouchResult : esptouchResultList)
            {
                // check whether the task is executed suc
                if (!esptouchResult.isSuc())
                {
                    break;
                }
                // for doActionRefreshStaDevices() can't find them,
                // so we can't get the info like deviceType, etc.
                // thus we can't make them added into staDeviceList
                String bssid = BSSIDUtil.restoreBSSID(esptouchResult.getBssid());
                IOTAddress iotAddress = new IOTAddress(bssid, esptouchResult.getInetAddress());
                iotAddress.setEspDeviceTypeEnum(EspDeviceType.NEW);
                IEspDeviceSSS iotAddressDevice = BEspDevice.createSSSDevice(iotAddress);
                iotAddressDevice.getDeviceState().clearState();
                String deviceName = BSSIDUtil.genDeviceNameByBSSID(bssid);
                iotAddressDevice.setName(deviceName);
                addDeviceAsyn(iotAddressDevice);
            }
        }
        else
        {
            doActionRefreshStaDevices(true);
        }
        return esptouchResultList;
    }
    
    private boolean doEsptouchTaskPrepare()
    {
        synchronized (mEsptouchLock)
        {
            if (mIsEsptouchCancelled)
            {
                mIsEsptouchCancelled = false;
                return false;
            }
            mActionDeviceEsptouch = new EspActionDeviceEsptouch();
            mIsEsptouchCancelled = false;
        }
        return true;
    }
    
    @Override
    public void cancelAllAddDevices()
    {
        synchronized (mEsptouchLock)
        {
            if (mActionDeviceEsptouch != null)
            {
                if (mActionDeviceEsptouch.isCancelled() || mActionDeviceEsptouch.isDone())
                {
                    mIsEsptouchCancelled = true;
                }
                else
                {
                    mActionDeviceEsptouch.cancel();
                }
            }
        }
        IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
        handler.cancelAllTasks();
    }
    

    @Override
    public void doneAllAddDevices()
    {
        synchronized (mEsptouchLock)
        {
            if (mActionDeviceEsptouch != null)
            {
                mActionDeviceEsptouch.done();
            }
        }
    }
    
    @Override
    public void doActionGroupCreate(String groupName)
    {
        IEspActionGroupEditDB action = new EspActionGroupEditDB();
        action.doActionGroupCreate(groupName, mUserKey);
        
        EspGroupHandler.getInstance().call();
    }
    
    @Override
    public void doActionGroupRename(IEspGroup group, String newName)
    {
        IEspActionGroupEditDB action = new EspActionGroupEditDB();
        action.doActionGroupRename(group, newName);
        
        EspGroupHandler.getInstance().call();
    }
    
    @Override
    public void doActionGroupDelete(IEspGroup group)
    {
        IEspActionGroupEditDB action = new EspActionGroupEditDB();
        action.doActionGroupDelete(group);
        
        EspGroupHandler.getInstance().call();
    }
    
    @Override
    public void doActionGroupDeviceMoveInto(IEspDevice device, IEspGroup group)
    {
        IEspActionGroupDeviceDB action = new EspActionGroupDeviceDB();
        action.doActionMoveDeviceIntoGroupDB(mUserKey, device, group);
        
        EspGroupHandler.getInstance().call();
    }
    
    @Override
    public void doActionGroupDeviceRemove(IEspDevice device, IEspGroup group)
    {
        IEspActionGroupDeviceDB action = new EspActionGroupDeviceDB();
        action.doActionRemoveDevicefromGroupDB(mUserKey, device, group);
        
        EspGroupHandler.getInstance().call();
    }
    
    private Object mNewDevicesLock = new Object();
    
    private SharedPreferences getNewActivatedSharedPre()
    {
        EspApplication app = EspApplication.sharedInstance();
        String fileName = EspStrings.Key.NAME_NEW_ACTIVATED_DEVICES;
        return app.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }
    
    @Override
    public Set<String> getNewActivatedDevices()
    {
        synchronized (mNewDevicesLock)
        {
            Set<String> defaultResult = new HashSet<String>();
            if (isLogin())
            {
                SharedPreferences shared = getNewActivatedSharedPre();
                return shared.getStringSet(getUserKey(), defaultResult);
            }
            else
            {
                return defaultResult;
            }
        }
    }
    
    @Override
    public void saveNewActivatedDevice(String deviceKey)
    {
        synchronized (mNewDevicesLock)
        {
            if (isLogin())
            {
                SharedPreferences shared = getNewActivatedSharedPre();
                Set<String> devices = shared.getStringSet(getUserKey(), new HashSet<String>());
                devices.add(deviceKey);
                shared.edit().putStringSet(getUserKey(), devices).commit();
            }
        }
    }
    
    @Override
    public void deleteNewActivatedDevice(String deviceKey)
    {
        synchronized (mNewDevicesLock)
        {
            if (isLogin())
            {
                SharedPreferences shared = getNewActivatedSharedPre();
                Set<String> devices = shared.getStringSet(getUserKey(), new HashSet<String>());
                Set<String> newDevices = new HashSet<String>();
                for (String key : devices)
                {
                    if (!key.equals(deviceKey))
                    {
                        newDevices.add(key);
                    }
                }
                shared.edit().putStringSet(getUserKey(), newDevices).commit();
            }
        }
    }
    
    @Override
    public boolean doActionEspButtonAdd(String tempKey, String macAddress, boolean permitAllRequest,
        List<IEspDevice> deviceList, boolean isBroadcast, IEspButtonConfigureListener listener)
    {
        return doActionEspButtonReplace(tempKey, macAddress, permitAllRequest, deviceList, isBroadcast, listener);
    }
    
    @Override
    public boolean doActionEspButtonReplace(String newTempKey, String newMacAddress, boolean permitAllRequest,
        List<IEspDevice> deviceList, boolean isBroadcast, IEspButtonConfigureListener listener, String... oldMacAddress)
    {
        IEspActionEspButtonConfigure action = new EspActionEspButtonConfigure();
        return action.doActionEspButtonConfigure(newTempKey,
            newMacAddress,
            permitAllRequest,
            deviceList,
            isBroadcast,
            listener,
            oldMacAddress);
    }
    
    @Override
    public boolean doActionEspButtonKeyActionSet(IEspDevice device, EspButtonKeySettings settings)
    {
        IEspActionEspButtonActionSet action = new EspActionEspButtonActionSet();
        return action.doActionEspButtonActionSet(device, settings);
    }
    
    @Override
    public List<EspButtonKeySettings> doActionEspButtonKeyActionGet(IEspDevice device)
    {
        IEspActionEspButtonActionGet action = new EspActionEspButtonActionGet();
        return action.doActionEspButtonActionGet(device);
    }

}
