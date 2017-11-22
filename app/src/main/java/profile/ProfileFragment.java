package profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.udacity.test.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

import objects.NetworkManager;
import objects.Post;
import objects.User;
import objects.UserSingleton;
import post.EditPost;
import post.SinglePostActivity;

/*****
 *
 * ProfileFragment is for owner, use ProfileActivity for poster
 * find and fix SinglePostActivity ProfileActivity
 *
 * *****/

public class ProfileFragment extends Fragment {
    private static final String TEXT = "text";
    private String userID;
    private RatingBar ratingBar;
    private String userid;
    private TextView name;
    private ListView postList;
    private UserSingleton owner;
    private NetworkManager networkManager;

    private RatingParser ratingParser;
    private TextView currRating;

    public ProfileFragment(){
        this.owner = UserSingleton.getUserInstance();
        this.networkManager = new NetworkManager();

        // does this work?????
        networkManager.getUser(this, userID);
        networkManager.getPostsForUser(userID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    public void generate (User user){
        name.setText(user.getName());
        ratingBar.setRating(user.getRating());
        userid = user.getId();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_profile,container,false);

        ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
        name = (TextView) v.findViewById(R.id.profilename);
        name.setText(owner.getName());
        currRating = (TextView) v.findViewById(R.id.profileRating);

        // get rating
        ratingParser = new RatingParser();
        ratingParser.getRatingWithID(owner.get_id(), this);

        // getting posts??
        ArrayList<Post> posts = owner.getPosts();

        ArrayList<Post> postsToShow = new ArrayList<Post>();
        for (int i=0; i<posts.size(); i++){
            Post thispost = posts.get(i);
            String temp = thispost.getUser().getId();
            if (temp.equals(userid) ){
                postsToShow.add(thispost);
            }
        }

        postList = (ListView) v.findViewById(R.id.posts);
        postList.setAdapter(new MyAdapterPost(postsToShow));

        return v;
    }

    public void setRating(float rating) {
        ratingBar.setRating(rating);
        currRating.setText(String.format("%.2f", rating));
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();

        // refresh rating
        ratingParser.getRatingWithID(owner.get_id(), this);
    }


    private class MyAdapterPost extends ArrayAdapter<Post> {
        ArrayList<Post> posts;
        public MyAdapterPost(ArrayList<Post> posts){
            super(getActivity(),0,posts);
            this.posts = posts;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.single_post,null);
            }
            final Post current = posts.get(position);

            ((TextView)convertView.findViewById(R.id.title)).setText(current.getTitle());
            ((TextView)convertView.findViewById(R.id.description)).setText(current.getDescription());
            ((TextView)convertView.findViewById(R.id.price)).setText(current.getPrice());
            ((TextView)convertView.findViewById(R.id.date)).setText(current.getDate());
            ((TextView)convertView.findViewById(R.id.address)).setText(current.getLocation());

//            Button editPost = (Button) convertView.findViewById(R.id.editpost);
//            editPost.setOnClickListener(new View.OnClickListener(){
//                @Override
//                public void onClick(View v){
//                    Intent newActivity = new Intent(getActivity(), EditPost.class);
//                    newActivity.putExtra("PostIndex", position);
//                    startActivity(newActivity);
//                }
//            });

            return convertView;
        }
    }
}