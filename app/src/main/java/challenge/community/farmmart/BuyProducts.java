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

public class BuyProducts extends AppCompatActivity {

    private ImageView callSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_products);

        callSeller = findViewById(R.id.callSeller);

        final String contact = getIntent().getExtras().getString("contact");

        callSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent callSeller = new Intent(Intent.ACTION_CALL);
                callSeller.setData(Uri.parse(contact));
                if (ActivityCompat.checkSelfPermission(BuyProducts.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callSeller);
            }
        });
    }
}
