package com.nowfloats.smartretailer.Activities;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.nowfloats.smartretailer.HelperClasses.BluetoothSPP;
import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.Models.Product_Table;
import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.Utils;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class AddProductActivity extends BaseActivity implements BluetoothSPP.OnDataReceivedListener {

    @BindView(R.id.etProductId)
    EditText etProductId;
    @BindView(R.id.etProductName)
    EditText etProductName;
    @BindView(R.id.etProductPrice)
    EditText etProductPrice;
    @BindView(R.id.etProductDiscount)
    EditText etProductDiscount;


    @Override
    public void initialize() {
        llContent.addView(mLayoutInflater.inflate(R.layout.activity_add_product, null));
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnProductSave)
    public void saveProduct(View v) {
        Product product = getProductDetails();
        if (product != null) {
            try {
                product.save();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    SQLite.update(Product.class)
                            .set(Product_Table.productName.eq(product.getProductName()),
                                    Product_Table.productPrice.eq(product.getProductPrice()),
                                    Product_Table.productDiscount.eq(product.getProductDiscount()))
                            .where(Product_Table.productId.is(product.getProductId()))
                            .async()
                            .execute();
                    Timber.d("Product Updated");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            Toast.makeText(this, getString(R.string.product_saved_text), Toast.LENGTH_SHORT).show();
            Utils.makeEditTextEmpty(new EditText[]{etProductId, etProductName, etProductPrice, etProductDiscount});
        } else {
            Toast.makeText(this, getString(R.string.alert_all_fields_not_filled), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showLoader(getString(R.string.please_wait));
        connectScanner(AddProductActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private Product getProductDetails() {
        String productId = etProductId.getText().toString().trim();
        String productName = etProductName.getText().toString().trim();
        String productPrice = etProductPrice.getText().toString().trim();
        String productDiscount = etProductDiscount.getText().toString().trim();
        if (!Utils.isNullOrEmpty(new String[]{productId, productName, productPrice, productDiscount})) {
            Product product = new Product();
            product.setProductId(productId);
            product.setProductName(productName);
            product.setProductCurrency("INR");
            product.setProductPrice(Double.parseDouble(productPrice));
            product.setProductDiscount(Double.parseDouble(productDiscount));
            return product;
        }
        return null;
    }

    @Override
    public void onDataReceived(byte[] data, String message) {
        etProductId.setText(message);
        SQLite.select()
                .from(Product.class)
                .where(Product_Table.productId.is(message))
                .async()
                .querySingleResultCallback(new QueryTransaction.QueryResultSingleCallback<Product>() {
                    @Override
                    public void onSingleQueryResult(QueryTransaction transaction, @Nullable Product product) {
                        if (product != null) {
                            etProductName.setText(product.getProductName());
                            etProductPrice.setText(product.getProductPrice() + "");
                            etProductDiscount.setText(product.getProductDiscount() + "");
                        }
                    }
                })
                .execute();
    }


}
