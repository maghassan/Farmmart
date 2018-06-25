package challenge.community.farmmart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BuyProducts extends AppCompatActivity {

    private ImageView callSeller;
    private TextView postT, postP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_products);

        callSeller = findViewById(R.id.callSeller);
        postT = findViewById(R.id.product_Title);
        postP = findViewById(R.id.product_Price);

        final String contact = getIntent().getExtras().getString("contact");
        final String title = getIntent().getExtras().getString("title");
        final String price = getIntent().getExtras().getString("price");

        postP.setText(price);
        postT.setText(title);

        callSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent callSeller = new Intent(Intent.ACTION_NEW_OUTGOING_CALL);
                callSeller.setData(Uri.parse("tel :" + contact));
                callSeller.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (ActivityCompat.checkSelfPermission(BuyProducts.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {


                }
                startActivity(callSeller);
            }
        });
    }
}
