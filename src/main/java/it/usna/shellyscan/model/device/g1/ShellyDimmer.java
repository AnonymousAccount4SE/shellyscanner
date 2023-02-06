package it.usna.shellyscan.model.device.g1;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;

import it.usna.shellyscan.model.Devices;
import it.usna.shellyscan.model.device.InternalTmpHolder;
import it.usna.shellyscan.model.device.Meters;
import it.usna.shellyscan.model.device.MetersPower;
import it.usna.shellyscan.model.device.g1.modules.LightWhite;
import it.usna.shellyscan.model.device.modules.WhiteCommander;

public class ShellyDimmer extends AbstractG1Device implements WhiteCommander, InternalTmpHolder {
	public final static String ID = "SHDM-1";
	private float internalTmp;
	private boolean calibrated;
	private LightWhite light = new LightWhite(this, "/light/", 0);
	private float power;
	private Meters[] meters;

	public ShellyDimmer(InetAddress address) {
		super(address);
		
		meters = new Meters[] {
				new MetersPower() {
					@Override
					public float getValue(Type t) {
						return power;
					}
				}
		};
	}

	@Override
	public String getTypeName() {
		return "Shelly Dimmer";
	}
	
	@Override
	public String getTypeID() {
		return ID;
	}
	
	@Override
	public int getWhiteCount() {
		return 1;
	}
	
	@Override
	public LightWhite getWhite(int index) {
		return light;
	}
	
	@Override
	public LightWhite[] getWhites() {
		return new LightWhite[] {light};
	}

	@Override
	public float getInternalTmp() {
		return internalTmp;
	}
	
	public float getPower() {
		return power;
	}
	
	@Override
	public Meters[] getMeters() {
		return meters;
	}
	
//	@Override
//	public void statusRefresh() throws IOException {
//		light.refresh();
//	}

	@Override
	protected void fillSettings(JsonNode settings) throws IOException {
		super.fillSettings(settings);
		light.fillSettings(settings.get("lights").get(0));
		calibrated = settings.get("calibrated").asBoolean();
	}
	
	protected void fillStatus(JsonNode status) throws IOException {
		super.fillStatus(status);
		light.fillStatus(status.get("lights").get(0), status.get("inputs").get(0));
		internalTmp = (float)status.at("/tmp/tC").asDouble(); //status.get("tmp").get("tC").asDouble();
		power = (float)status.get("meters").get(0).get("power").asDouble(0);
	}
	
	public boolean calibrated() {
		return calibrated;
	}
	
//	@Override
//	public String[] getInfoRequests() {
//		return new String[] {"shelly", "settings", "settings/actions", "light/0", "status", "ota"};
//	}

	@Override
	protected void restore(JsonNode settings, ArrayList<String> errors) throws IOException, InterruptedException {
		errors.add(sendCommand("/settings?" + jsonNodeToURLPar(settings, "led_status_disable", "factory_reset_from_switch", "pulse_mode", "transition", "fade_rate", "min_brightness", "zcross_debounce")));
		TimeUnit.MILLISECONDS.sleep(Devices.MULTI_QUERY_DELAY);
		JsonNode nightMode = settings.get("night_mode");
		if(nightMode.get("enabled").asBoolean()) {
			errors.add(sendCommand("/settings/night_mode?" + jsonNodeToURLPar(nightMode, "enabled", "start_time", "end_time", "brightness")));
		} else {
			errors.add(sendCommand("/settings/night_mode?enabled=false"));
		}
		TimeUnit.MILLISECONDS.sleep(Devices.MULTI_QUERY_DELAY);
		JsonNode warmUp = settings.get("warm_up");
		if(warmUp.get("enabled").asBoolean()) {
			errors.add(sendCommand("/settings/warm_up?" + jsonNodeToURLPar(warmUp, "enabled", "brightness", "time")));
		} else {
			errors.add(sendCommand("/settings/warm_up?enabled=false"));
		}
		TimeUnit.MILLISECONDS.sleep(Devices.MULTI_QUERY_DELAY);
		errors.add(light.restore(settings.get("lights").get(0)));
	}

	@Override
	public String toString() {
		return super.toString() + " Load: " + light;
	}
}