# How to use Bitcoin Wallet Viewer #

These steps are for beginner users, if you want to manually export your keys, or compile the decoder from source, or use another key export utility, please visit the <a href='http://code.google.com/p/bitcoinwallet/wiki/FAQ'>FAQ</a>.

**Step 1: download the decoder**

Go <a href='http://code.google.com/p/bitcoinwallet/downloads/list'>here</a> and download the decoder executable for your platform.  You can also export your addresses using <a href='https://github.com/gavinandresen/bitcointools'>bitcointools</a> or <a href='https://github.com/joric/pywallet'>pywallet</a> to manually create a text file with all your addresses.

**Step 2: extract the keys**

Run the decoder or python script e.g.
```
decode %appdata%\bitcoin\wallet.dat > keys.txt
```
or

```
./decode ~/.bitcoin/wallet.dat > keys.txt
```

```
pywallet.py --dump-wallet > keys.txt
```

Examine the keys.txt - it should start with '1' and should be 33 or 34 characters long.   If you've used pywallet then consider removing the private keys before copying to your phone (for security!)

**Step 3: Upload the keys**

Connect your Android phone over USB, and copy the keys.txt to the SD card.  This should be the top level folder on Windows, or `/sdcard` on some systems.

**Step 4: Install the android application from the market**

The application is available <a href='https://market.android.com/details?id=net.phase.wallet'>here</a>

**Step 5: Import the keys**

Select **Options->Import from File...** and your key file should appear in `/mnt/sdcard/keyXXX.txt`.  If it doesn't appear, check the name is keyXXX.txt where XXX can be anything you like, and that it's in the root of the SD card.

**Step 6: Update your balance**

Click **Update** next to your wallet status, or long click->Force Update to force an update of all keys, or options->Update all if you have multiple wallets.

**All done!**  The balance will now update automatically as you pay bitcoins in/out of your wallet.  If you just receive bitcoins e.g. from your pool, you can now secure your wallet.dat on a USB stick and check your balance completely securely from your Android phone