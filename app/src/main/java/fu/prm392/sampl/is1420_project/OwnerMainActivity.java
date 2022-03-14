package fu.prm392.sampl.is1420_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import fu.prm392.sampl.is1420_project.fragments.OwnerHomeFragment;
import fu.prm392.sampl.is1420_project.fragments.OwnerManagerFragment;
import fu.prm392.sampl.is1420_project.fragments.UserHomeFragment;

public class OwnerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch(item.getItemId()){
                    //check id
                    case R.id.pageHome:
                        selectedFragment = new OwnerHomeFragment();
                        break;
                    case R.id.pageManager:
                        selectedFragment = new OwnerManagerFragment();
                        break;
                    default:
                        return false;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
                return true;
            }
        });

        //default fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new OwnerHomeFragment()).commit();
    }
}