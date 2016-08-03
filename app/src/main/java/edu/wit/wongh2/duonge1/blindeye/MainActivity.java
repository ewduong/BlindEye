package edu.wit.wongh2.duonge1.blindeye;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // UI elements
    private Toolbar toolbar;
    private ViewPager pager;
    private ViewPagerAdapter viewPagerAdapter;
    private SlidingTabLayout tabs;
    private CharSequence titles[] = {"Settings","Home", "Logs"};
    private int numTabs = 3;

    // "radar" objects
    // CircularProgressDrawable circle;
    //private ImageView ivDrawable;

    // BTLE state
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    private boolean bluetoothEnabled;

    // Main BTLE device callback where much of the logic occurs.
    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                //writeLine("Connected!");
                Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
                // Discover services.
                if (!gatt.discoverServices()) {
                    //writeLine("Failed to start discovering services!");
                    Toast.makeText(getApplicationContext(), "Failed to start discovering services!", Toast.LENGTH_SHORT).show();
                }
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                //writeLine("Disconnected!");
                Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
            }
            else {
                //writeLine("Connection state changed.  New state: " + newState);
                Toast.makeText(getApplicationContext(), "Connection state changed.  New state: " + newState, Toast.LENGTH_SHORT).show();
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //writeLine("Service discovery completed!");
            }
            else {
                //writeLine("Service discovery failed with status: " + status);
            }

            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);

            Log.v("BLE", "tx:" + tx);
            Log.v("BLE", "rx:" + rx.getDescriptor(CLIENT_UUID) );

            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                //writeLine("Couldn't set notifications for RX characteristic!");
            }

            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    //writeLine("Couldn't write RX client descriptor value!");
                }
            }
            else {
                //writeLine("Couldn't get RX client descriptor!");
            }
        }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //writeLine("Received: " + characteristic.getStringValue(0));

            /*
            Log.v("BLE", "onCharacteristChanged is called. value = " + characteristic.getStringValue(0));
            writeSensorData(characteristic.getStringValue(1));
            */
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothEnabled = isBluetoothAvailable();

        //messages = (TextView) findViewById(R.id.messages);
        if (bluetoothEnabled) {
            adapter = BluetoothAdapter.getDefaultAdapter();
        }

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.eye);

        // Example of progress circle
        //ivDrawable = (ImageView) findViewById(R.id.sensorView);
        //Toast.makeText(getApplicationContext(), ivDrawable.getHeight(), Toast.LENGTH_LONG).show();

        /*circle = new CircularProgressDrawable.Builder()
                .setRingWidth(getResources().getDimensionPixelSize(R.dimen.drawable_ring_size))
                .setOutlineColor(getResources().getColor(android.R.color.darker_gray))
                .setCenterColor(getResources().getColor(android.R.color.holo_blue_dark))
                .create();
        try {
            ivDrawable.setImageDrawable(circle);
        } catch (NullPointerException e) {
            Log.e("FUCK", e.getMessage());
        }

        progressCircleAnimation().start();*/

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        viewPagerAdapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles, numTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(viewPagerAdapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor, getTheme());
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
        // End example

    }

    // OnResume, called right before UI is displayed.  Start the BTLE connection.
    @Override
    protected void onResume() {
        super.onResume();
        // Scan for all BTLE devices.
        // The first one with the UART service will be chosen--see the code in the scanCallback.
        //writeLine("Scanning for devices...");
        if (bluetoothEnabled) {
            Toast.makeText(getApplicationContext(), "Scanning for BlindEye", Toast.LENGTH_SHORT).show();
            adapter.startLeScan(scanCallback);
        } else {
            Toast.makeText(getApplicationContext(), "There is no Bluetooth available!", Toast.LENGTH_SHORT).show();
        }
    }

    // OnStop, called right before the activity loses foreground focus.  Close the BTLE connection.
    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothEnabled) {
            if (gatt != null) {
                // For better reliability be careful to disconnect and close the connection.
                gatt.disconnect();
                gatt.close();
                gatt = null;
                tx = null;
                rx = null;
            }
        }
    }

    // Write some text to the messages text view.
    // Care is taken to do this on the main UI thread so writeLine can be called
    // from any thread (like the BTLE callback).
    /*private void writeLine(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.append(text);
                messages.append("\n");

                messages.invalidate();
            }
        });
    }*/

    // Filtering by custom UUID is broken in Android 4.3 and 4.4, see:
    //   http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation?noredirect=1#comment27879874_18019161
    // This is a workaround function from the SO thread to manually parse advertisement data.
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            //Log.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }

    // BTLE device scanning callback.
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        // Called when a device is found.
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {

            if (bluetoothDevice.getName() == "blind") {
                adapter.stopLeScan(scanCallback);
                gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
                Toast.makeText(getApplicationContext(), "Found BlindEye service!", Toast.LENGTH_SHORT).show();
            }
            //writeLine("Found device: " + bluetoothDevice.getAddress());
            // Check if the device has the UART service.
            /*if (parseUUIDs(bytes).contains(UART_UUID)) {
                // Found a device, stop the scan.
                adapter.stopLeScan(scanCallback);
                //writeLine("Found UART service!");
                Toast.makeText(getApplicationContext(), "Found UART service!", Toast.LENGTH_SHORT).show();
                // Connect to the device.
                // Control flow will now go to the callback functions when BTLE events occur.
                gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
            }*/
        }
    };

    /*private Animator progressCircleAnimation() {
        AnimatorSet animation = new AnimatorSet();

        final Animator innerCircleAnimation = ObjectAnimator.ofFloat(circle, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 0f, 1f);
        innerCircleAnimation.setDuration(3600);
        Animator innerCircleAnimationEnd = ObjectAnimator.ofFloat(circle, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY, 1f, 0f);
        innerCircleAnimationEnd.setDuration(3600);

        innerCircleAnimationEnd.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                circle.setIndeterminate(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //indeterminateAnimation.end();
                //circle.setIndeterminate(false);
                //circle.setProgress(0);
                progressCircleAnimation().start();
            }
        });

        animation.playSequentially(innerCircleAnimation, innerCircleAnimationEnd);

        return animation;
    }*/

    /**
     * Check for Bluetooth.
     * @return True if Bluetooth is available.
     */
    public static boolean isBluetoothAvailable() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }
}
