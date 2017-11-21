package post;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.udacity.test.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import objects.NetworkManager;
import objects.Parser;
import objects.Post;
import objects.User;
import objects.UserSingleton;
import profile.ProfileActivity;


public class SinglePostFragment extends Fragment {
    private static final String TEXT = "postIndex";
    private int PostIndex;
    FrameLayout fragmentContainer ;
    Button myButton;
    UserSingleton user;
    Post post;
    NetworkManager networkManager;
    // SinglePostFragment.OnFragmentInteractionListener mListener;

    public SinglePostFragment() {
        // Required empty public constructor
    }

    public static SinglePostFragment newInstance(int PostIndex){
        SinglePostFragment fragment = new SinglePostFragment();
        Bundle args = new Bundle();
        args.putInt(TEXT, PostIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            PostIndex = getArguments().getInt(TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_single_post, container, false);
        user = UserSingleton.getUserInstance();
        post = user.getPosts().get(PostIndex);

        // TODO change stuff around cuz u know
        ImageView profilepic = (ImageView) v.findViewById(R.id.profilepic);
        // ImageView picture = (ImageView) findViewById(R.id.picture);


        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(post.getTitle());
        TextView date = (TextView) v.findViewById(R.id.date);
        date.setText(post.getDate());
        TextView price = (TextView) v.findViewById(R.id.price);
        price.setText(String.valueOf(post.getPrice()));
        TextView description = (TextView) v.findViewById(R.id.description);
        description.setText(post.getDescription());
        TextView address = (TextView) v.findViewById(R.id.address);
        address.setText(post.getLocation());
        TextView num = (TextView) v.findViewById(R.id.numAvailable);
        num.setText(String.valueOf(post.getAvailable()));


        Button request = (Button) v.findViewById(R.id.requestButton);
        request.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                EditText location = (EditText) v.findViewById(R.id.locationEdit);
                if(location == null){
                    System.out.println("the fuck?");
                }else{
                    String loc = location.getText().toString();
                }
                String loc = "hi";
                RequestNetwork rn = new RequestNetwork(UserSingleton.getUserInstance().get_id(),
                        post.get_id(),loc);
                rn.execute();
                TextView tv = (TextView) v.findViewById(R.id.update);
                //tv.setText("request sent");
            }
        });

//        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        profilepic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
//                User op = post.getUser();
//                String userID = op.getId();
//
//                ((SinglePostActivity) getActivity()).change(false);

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                User op = post.getUser();
                String userID = op.getId();
                intent.putExtra("userid", userID);
                startActivity(intent);
            }

        });

        return v;
    }



    private class RequestNetwork extends AsyncTask<String,Void,Void>{
        String userid;
        String postid;
        String location;
        public RequestNetwork(String userid, String postid, String location){
            this.userid = userid;
            this.postid = postid;
            this.location = location;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String urlString = String.format(
                    "https://grubmateteam3.herokuapp.com/api/posts?personid=%s&postid=%s&type=request&location=%s",
                    userid, postid, location);
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.connect();
                Parser p = new Parser();
                System.out.println(p.convertStreamToString(connection.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
