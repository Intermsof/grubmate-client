package profile;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.udacity.test.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import objects.NetworkManager;
import objects.Parser;
import objects.Post;
import objects.User;
import objects.UserSingleton;
import post.EditPost;

/*****
 *
 * ProfileFragment is for owner, use ProfileActivity for poster
 * find and fix SinglePostActivity ProfileActivity
 *
 * *****/

public class ProfileFragment extends Fragment {
    private static final String TEXT = "text";
    private RatingBar ratingBar;
    private TextView name;
    private ListView postList;
    private UserSingleton owner;
    private MyAdapterPost adapter;
    private RatingParser ratingParser;
    private TextView currRating;
    private int numPosts;

    public ProfileFragment(){
        this.owner = UserSingleton.getUserInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }


    private class PostParser extends AsyncTask<String,Void,Void>{
        ProfileFragment f;
        ArrayList<Post> result = new ArrayList<>();
        public PostParser(ProfileFragment f){
            System.out.println("CALLING POST SHIT");
            this.f = f;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (result==null || result.isEmpty()) {
                System.out.println("resultisfucked");
            } else {
                System.out.println("resultismorefucked");
            }
            f.setPosts(result);
        }

        @Override
        protected Void doInBackground(String... strings) {
            try{
                URL url = new URL("https://grubmateteam3.herokuapp.com/api/user?userid="
                        + UserSingleton.getUserInstance().get_id());

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();
                JsonReader reader = new JsonReader(new InputStreamReader(is,"UTF-8"));
                reader.beginObject();
                ArrayList<String> postsIds = new ArrayList<>();
                while(reader.hasNext()){
                    String name = reader.nextName();
                    if(!name.equals("postsOfUser")){
                        reader.skipValue();
                    }else{
                        reader.beginArray();
                        while(reader.hasNext()) {
                            postsIds.add(reader.nextString());
                        }
                        reader.endArray();
                    }
                }
                reader.endObject();

                for(String id : postsIds){
                    URL url2 = new URL("https://grubmateteam3.herokuapp.com/api/singlepost?postid="
                        + id);

                    HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                    conn2.setRequestMethod("GET");
                    conn2.connect();

                    InputStream is2 = conn2.getInputStream();

                    Parser p = new Parser();
                    Post post = p.parsePost(is2);

                    result.add(post);
                }

            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_profile,container,false);

        ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
        name = (TextView) v.findViewById(R.id.profilename);
        name.setText(owner.getName());
        currRating = (TextView) v.findViewById(R.id.profileRating);
        numPosts = 0;

        // get rating
        ratingParser = new RatingParser();
        ratingParser.getRatingWithID(owner.get_id(), this);

        PostParser pp = new PostParser(this);
        pp.execute();

        postList = (ListView) v.findViewById(R.id.posts);
        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newActivity = new Intent(getActivity(), EditPost.class);
                newActivity.putExtra("Post", (Post)parent.getAdapter().getItem(position));
                startActivity(newActivity);
            }
        });

        // for text reviews, click on number rating
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity().getApplicationContext(), ReviewActivity.class);
                Bundle b = new Bundle();
                b.putString("userid", owner.get_id());
                intent.putExtras(b);
                startActivity(intent);

            }
        });

        return v;
    }

    public void setRating(float rating) {
        ratingBar.setRating(rating);
        currRating.setText(String.format("%.2f", rating));
    }

    private void setPosts(ArrayList<Post> p){
        postList = (ListView) getView().findViewById(R.id.posts);
        adapter = new MyAdapterPost((p));
        postList.setAdapter(adapter);
        postList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newActivity = new Intent(getActivity(), EditPost.class);
                newActivity.putExtra("Post", (Post)parent.getAdapter().getItem(position));
                startActivity(newActivity);
            }
        });

        // updates num posts
        numPosts = p.size();
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();

        // refresh rating
        ratingParser.getRatingWithID(owner.get_id(), this);

        // adds post if necessary
        int currNumPosts = owner.getNumPosts();
        if (numPosts<currNumPosts) {
            Post newPost = owner.getPosts().get(currNumPosts-1);
            adapter.addPost(newPost);
            numPosts+=1;
        }
    }

    private class MyAdapterPost extends ArrayAdapter<Post> {
        ArrayList<Post> posts;
        public MyAdapterPost(ArrayList<Post> posts){
            super(getActivity(),0,posts);
            this.posts = posts;
        }

        public void addPost(Post post) {
            if (post!=null) {
                this.posts.add(post);
                notifyDataSetChanged();
            }
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

            return convertView;
        }
    }
}