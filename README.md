# Medicine POS for Android — starter project

A native Android starter app written in Kotlin + Jetpack Compose for a small medicine shop.

## Included in this demo
- Medicine search by name, brand, or batch
- Sample medicine catalog with price, stock, batch, and expiry
- Add items to a cart and adjust quantities
- Basic checkout confirmation and INR total
- Inventory overview and low-stock indicator
- Reports screen placeholder

## Important limitations
This is a **starter/demo**, not ready for live pharmacy billing. It currently uses in-memory sample data:
- Products and transactions are not saved after closing the app.
- Checkout does not permanently reduce stock or create a printable tax invoice.
- GST, barcode scanning, supplier purchases, returns, user login, and cloud backup are not implemented.
- Expiry dates are displayed but not automatically validated.
- Confirm local legal and pharmacy requirements before using in a real shop.

## Open and run
1. Install Android Studio (current stable version).
2. Extract this ZIP.
3. In Android Studio, choose **Open** and select the `MedicinePOSAndroid` folder.
4. Allow Gradle to sync, then run on an Android device or emulator (Android 7.0/API 24 or later).

## Suggested next production features
- Room/SQLite persistence and backup/restore
- Product CRUD with batch-wise stock and expiry alerts
- Barcode scanner and quick checkout
- GST invoice number, discount, tax, payment mode and receipt printing
- Purchase entry, supplier ledger, sales returns and stock adjustments
- Sales/profit reports and CSV export
