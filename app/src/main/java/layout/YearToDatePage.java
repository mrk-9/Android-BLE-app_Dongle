package layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.filter.FilterOperation;
import com.belladati.sdk.filter.FilterValue;
import com.belladati.android.views.Kpi;
import com.example.karimt.belladatidongleapp.ConnectBTActivity;
import com.example.karimt.belladatidongleapp.MainActivity;
import com.example.karimt.belladatidongleapp.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Created by KarimT on 30.09.2016.
 */
public class YearToDatePage extends Fragment {
    Attribute a;
    Filter.MultiValueFilter fUserId;
    Kpi dashboard;

    public YearToDatePage() {
    }

    public static YearToDatePage newInstance() {
        YearToDatePage fragment = new YearToDatePage();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_yeartodate, container, false);
        super.onCreate(savedInstanceState);
        if (ConnectBTActivity.internetConnection &&!MainActivity.savedDongleName.equals("")) {
            createFilter("L_SENSOR_ID", "32949");
            fUserId = FilterOperation.IN.createFilter(a);
            fUserId.addValue(new FilterValue(MainActivity.savedDongleName+" - Summary"));

            ObjectNode filterNode = new ObjectMapper().createObjectNode();
            filterNode.setAll(fUserId.toJson());

            dashboard = (Kpi) rootView.findViewById(R.id.linearMain);
            dashboard.setBackground(getResources().getDrawable(R.drawable.linear_style));
            dashboard.setTextColor(R.color.darkBlue);
            dashboard.setService(MainActivity.service);
            dashboard.setFilterNode(filterNode);
            dashboard.setIdKpi("48328-rCtBF5PraV");
            try {
                dashboard.fillKpi();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
        return rootView;
    }

    //filter for download data
    private void createFilter(String filter, String idDataset) {
        a = null;
        for (Attribute aa : MainActivity.service.loadDataSet(idDataset).getAttributes()) {
            if (filter.equalsIgnoreCase(aa.getCode())) {
                a = aa;
                break;
            }
        }
    }

}
