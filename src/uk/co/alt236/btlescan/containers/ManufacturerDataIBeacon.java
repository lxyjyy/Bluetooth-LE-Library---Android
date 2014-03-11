package uk.co.alt236.btlescan.containers;

import java.util.Arrays;
import java.util.UUID;

import uk.co.alt236.btlescan.util.ByteUtils;
import android.util.Log;

public final class ManufacturerDataIBeacon {
	// 0	4C # Byte 1 (LSB) of Company identifier code
	// 1	00 # Byte 0 (MSB) of Company identifier code (0x004C == Apple)
	// 2	02 # Byte 0 of iBeacon advertisement indicator
	// 3	15 # Byte 1 of iBeacon advertisement indicator
	// 4	e2 |\
	// 5	c5 |\\
	// 6	6d |#\\
	// 7	b5 |##\\
	// 8	df |###\\
	// 9	fb |####\\
	// 10	48 |#####\\
	// 11	d2 |#####|| iBeacon proximity UUID
	// 12	b0 |#####||
	// 13	60 |#####//
	// 14	d0 |####//
	// 15	f5 |###//
	// 16	a7 |##//
	// 17	10 |#//
	// 18	96 |//
	// 19	e0 |/
	// 20	00 # major
	// 21	00
	// 22	00 # minor
	// 23	00
	// 24	c5 # The 2's complement of the calibrated Tx Power

	private final byte[] mData;
	private final int mCalibratedTxPower;
	private final int mCompanyIdentidier;
	private final int mIBeaconAdvertisment;
	private final int mMajor;
	private final int mMinor;
	private final UUID mUUID;

	public ManufacturerDataIBeacon(BluetoothLeDevice device){
		this(device.getAdRecordStore().getRecord(AdRecord.TYPE_MANUFACTURER_SPECIFIC_DATA).getData());
	}

	public ManufacturerDataIBeacon(byte[] data){
		mData = data;
		Log.d("TAG", "~ Reading iBeacon Data: " + ByteUtils.byteArrayToHexString(data));

		mCompanyIdentidier = ByteUtils.getIntFrom2ByteArray(
				ByteUtils.invertArray(
						Arrays.copyOfRange(mData, 0, 2)));

		mIBeaconAdvertisment = ByteUtils.getIntFrom2ByteArray(Arrays.copyOfRange(mData, 2, 4));
		mUUID = UUID.nameUUIDFromBytes(Arrays.copyOfRange(mData, 4, 20));
		mMajor = ByteUtils.getIntFrom2ByteArray(Arrays.copyOfRange(mData, 20, 22));
		mMinor = ByteUtils.getIntFrom2ByteArray(Arrays.copyOfRange(mData, 22, 24));
		mCalibratedTxPower = ByteUtils.getIntFromByte(data[24]);
	}

	public int getCalibratedTxPower(){
		return mCalibratedTxPower;
	}

	public int getCompanyIdentifier(){
		return mCompanyIdentidier;
	}

	public int getIBeaconAdvertisement(){
		return mIBeaconAdvertisment;
	}

	public int getMajor(){
		return mMajor;
	}

	public int getMinor(){
		return mMinor;
	}

	public UUID getUUID(){
		return mUUID;
	}

	// Code taken from: http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing
	protected static double calculateAccuracy(int txPower, double rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}

		double ratio = rssi*1.0/txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio,10);
		}
		else {
			final double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
			return accuracy;
		}
	}
}
