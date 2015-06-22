

package org.es25.wififingerprint;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import org.es25.wififingerprint.struct1.LocationMap;
import org.es25.wififingerprint.struct1.Station;


public class WifiFingerPrintDaemon extends IntentService {
	static private WifiManager wifimgr;
	public static final String NOTIFICATION = "org.es25.wififingerprint";
	public static final String APRESULT = "apresult";
	private static final String TAG = "WifiFingerPrintDaemon";
	private static final String EST_LOG_FILE = "RssiLogFile.csv";
	private static final String LOC_MAP_FILE = "RssiLearningMap.csv";
	private LocationMap learnedLocations;
	private List<ScanResult> apList = null;

	private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// WifiManager wifiMan=(WifiManager).getSystemService(Context.WIFI_SERVICE);
			// wifiMan.startScan();
			// int newRssi = wifiMan.getConnectionInfo().getRssi();
			// Toast.makeText(getActivity(), ""+newRssi, Toast.LENGTH_SHORT).show();

			apList = wifimgr.getScanResults();
			System.out.println(apList);

		}
	};


	/**
	 * Construct the shit.
	 */
	public WifiFingerPrintDaemon() {
		super("org.es25.wififingerprint.WifiFingerPrintDaemon");
	}


	@Override
	protected void onHandleIntent(Intent arg0) {
		try {
			learnedLocations = RssiUtils.loadMap(openFileInput(LOC_MAP_FILE));
		} catch (FileNotFoundException ex) {
			System.out.println("ERROR !! - Perform some learning scans first!");
		}

		Log.d(TAG, "========START WIFI-ING============");
		wifimgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifimgr.setWifiEnabled(true);
		registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		////  START SCANNING AND FILTERING THE SHIT
		//////////////////////////////////////////////////////////////////////
		Log.d(TAG, "========START WIFI-SCAN============");
		wifimgr.startScan();
		Set<Station> stations = RssiUtils.filterScan(wifimgr.getScanResults());

		System.out.println();
		System.out.println("LEARNED LOCATION MAP\n====================================================================");
		System.out.println(learnedLocations.getNames());
		System.out.println();

		System.out.println();
		System.out.println("CURRENT SCAN RESULTS\n====================================================================");
		System.out.println(stations);
		System.out.println();

		// TODO place some triangulation stuff here!!!
		//System.out.println("Intersect SET");
		//RssiUtils.calcEucliDist(readCsv(), currentLocations);
	}


	@Override
	public void onDestroy() {
		unregisterReceiver(wifiReceiver);

	}
}
