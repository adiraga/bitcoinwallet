### If you are looking for a quick start guide to using the Wallet Balance Viewer go <a href='http://code.google.com/p/bitcoinwallet/wiki/Using'>here</a>, otherwise read on... ###

<p><b>Q</b> <b>What does this application do?</b>
<p><b>A</b> It allows you to view your bitcoin balance on your Android phone.<br>
<br>
<p><b>Q</b> <b>What is bitcoin?</b>
<p><b>A</b> Bitcoin is a emerging distributed currency.  Read more about it <a href='http://www.weusecoins.com/'>here</a>

<p><b>Q</b> <b>Why would I want to use this?</b>
<p><b>A</b> Besides the obvious advantage of being able to view your balance while you are out and about, only the <b>PUBLIC</b> keys are required to view balance, so you can safely export them form your wallet, then store your wallet.dat in a secure place (e.g. encrypted media) while still being able to monitor for transactions on your wallet (e.g. your pool payments).  This means your bitcoins are <b>more secure</b>.  In addition, simply retrieving the transactions uses <b>less bandwidth</b> and takes <b>less time</b> than using the bitcoin client.<br>
<br>
<p><b>Q</b> <b>How does it work?</b>
<p><b>A</b> By using <a href='http://blockexplorer.com'>blockexplorer</a> to query for transactions to/from your bitcoin addresses.<br>
<br>
<p><b>Q</b> <b>Does it update automatically?</b>
<p><b>A</b> Yes, it will update with your current balance within a few seconds of the block containing your transaction being solved.  <b>You only need to export your addresses once</b> (but see below).<br>
<br>
<p><b>Q</b> <b>Couldn't I just go onto block explorer and type in my addresses?</b>
<p><b>A</b> Yes, but you wouldn't get your complete balance because of change transactions and outgoing payments.  Only by using all your addresses can you obtain an accurate balance.  Also it's a pain to have to type all your addresses into block explorer manually!<br>
<br>
<p><b>Q</b> <b>So you need my addresses?</b>
<p><b>A</b> Yes, you need to export your addresses from your wallet for the tool to be able to work.<br>
<br>
<p><b>Q</b> <b>How do I export my addresses?</b>
<p><b>A</b> I provide a tool (written in C) that can parse the addresses from your wallet and print them out.  You can also download a binary from <a href='http://code.google.com/p/bitcoinwallet/downloads/list'>here</a> if you don't want to compile from source.  If you have a large number of keys, pipe the output to a file so it can be easily uploaded to your phone e.g. <code>wallet &gt; keys.txt</code>

<p><b>Q</b> <b>Do I have to use your tool</b>
<p><b>A</b> No - but because bitcoin doesn't allow you to export all your addresses at the moment they must be exported manually.  The developers are working on a <a href='https://github.com/bitcoin/bitcoin/pull/220'>patch</a> to support it but until then it has to be done manually.   If you don't want to use my tool then there are other key export tools available<br>
<ul><li><a href='https://github.com/gavinandresen/bitcointools'>bitcointools</a> by Gavin Andresen<br>
</li><li><a href='https://github.com/joric/pywallet'>pywallet</a> by joric</li></ul>

<p><b>Q</b> <b>Why can't I just copy all the addresses from bitcoin manually</b>
<p><b>A</b> Because when you spend bitcoins the change from the transaction is sent back to hidden pool addresses that the bitcoin client doesn't show in the main UI - if you only took the addresses from the UI then your balance would show as too low depending on how many bitcoin sends you have done.  If you have never sent any bitcoins from a wallet, then this method would work.<br>
<br>
<p><b>Q</b> <b>How do I get my addresses to my Android phone?</b>
<p><b>A</b> You have two methods available to you.  You can either upload your addresses to pastebin, and the phone pulls them from there, or put them in a text file, copy them to your phone and then import from there.  You should call the file <code>keyXXX.txt</code> where <code>XXX</code> can be anything e.g. <code>keybob.txt</code> in the root of your SD card.<br>
<br>
<p><b>Q</b> <b>Should I worry about my addresses?</b>
<p><b>A</b> Although your addresses contain no information that would allow people to take your bitcoins, they do contain information that would allow people to track your transactions.  If you feel that your addresses have been compromised then you can always set up a new wallet and transfer your balance to that.<br>
<br>
<p><b>Q</b><b>I've uploaded my addresses to pastebin, what should I type into the import from URL box in the application?</b>
<p><b>A</b> You need to type the pastebin 'hash' which is the sequence of letters and numbers that pastebin gives you. e.g. L3LfHRP8<br>
<br>
<p><b>Q</b><b>But it's a real pain to enter the string!</b>
<p><b>A</b> Yes I know but you only need to do it once since it remembers the address.  Also, I'm working on adding QR code scanning support so you can quickly enter the address into your phone.  Alternatively, you can copy the addresses to your phone's SD card and call them keyXXX.txt<br>
<br>
<p><b>Q</b> <b>The balance is showing as incorrect - what's the problem?</b>
<p><b>A</b> The balance can be wrong for several reasons:<br>
<ul><li>either blockexplorer hasn't seen your transaction yet, this will happen for unconfirmed transactions (ones with 'unconfirmed/0' on them).  In addition, blockexplorer takes a few seconds after the first confirmation to database the transaction.<br>
</li><li>or, you have recently made a payment, then try forcing an update by long clicking on your wallet and clicking <b>Force Update</b>.  This makes the app query all your public keys and not just the ones with active transactions.<br>
</li><li>If you are behind a proxy it's possible that the long GET requests the application uses to query blockexplorer are being filtered - if this is the case then try reducing the 'addresses per request' in the options panel.<br>
</li><li>Make sure you are using the latest version (1.4.0)<br>
</li><li>If all else fails, it's possible that your bitcoin client has created a new address to receive the change (which it does, from time to time) and your bitcoins have been sent to one of these addresses.  In this case, you need to re-export your keys for the application to know where your bitcoins are.</li></ul>

<p><b>Q</b> <b>I have a cool idea for a new feature!</b>
<p><b>A</b> Please email me on will@phase.net with any suggestions you have, or post on the bitcoin forum <a href='http://forum.bitcoin.org/index.php?topic=21969'>here</a>.<br>
<br>
<p><b>Q</b> <b>I love your tool but don't have any bitcoins, can you send me some?</b>
<p><b>A</b> You can get free bitcoins from the <a href='https://freebitcoins.appspot.com/'>Bitcoin faucet</a>.  If you don't have any luck with that, pop me a message and I might be able to sort you out with some!<br>
<br>
<p><b>Q</b> <b>How can I donate to you?</b>
<p><b>A</b> Any donations, however small, are appreciated - just send them to 1NbXdG4Bu9LFpF3YNnT1LtZNqJroQrVqmg