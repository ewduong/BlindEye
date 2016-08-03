package edu.wit.wongh2.duonge1.blindeye;

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
import android.widget.Toast;

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

        if (bluetoothEnabled) {
            adapter = BluetoothAdapter.getDefaultAdapter();
        }

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.eye);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        viewPagerAdapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles, numTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(viewPagerAdapter);
        pager.setCurrentItem(1);

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

    // BTLE device scanning callback.
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        // Called when a device is found.
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {

            // Check if the device has the UART service.
            if (bluetoothDevice.getName() == "blind") {
                adapter.stopLeScan(scanCallback);
                gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
                Toast.makeText(getApplicationContext(), "Found BlindEye service!", Toast.LENGTH_SHORT).show();
            }
            //writeLine("Found device: " + bluetoothDevice.getAddress());
        }
    };

    /**
     * Check for Bluetooth.
     * @return True if Bluetooth is available.
     */
    public static boolean isBluetoothAvailable() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }
}
