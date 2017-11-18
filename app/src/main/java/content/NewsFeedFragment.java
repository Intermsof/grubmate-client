package content;

/**
 * Created by Alex Pan on 10/17/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import post.MakePost;
import com.example.udacity.test.R;
import post.SinglePostActivity;

import java.util.ArrayList;

import objects.NetworkManager;
import objects.Post;
import objects.UserSingleton;

public class NewsFeedFragment extends android.support.v4.app.Fragment{
    UserSingleton owner;
    EditText searchfield;
    Button filter;
    Button newpost;
    ListView listposts;
    NetworkManager networkManager;

    myAdapterPost adapter;
    Button refresh;


    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        owner = UserSingleton.getUserInstance();
        networkManager = new NetworkManager(this);
        ArrayList<String> postIds = owner.getPostIds();

        adapter = new myAdapterPost(owner.getPosts());
        networkManager.getPostsForUser(owner.get_id());

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b){
        System.out.println("This is calledd");
        View v = inflater.inflate(R.layout.fragment_newsfeed,container,false);

        searchfield = (EditText)v.findViewById(R.id.searchfield);
        filter = (Button)v.findViewById(R.id.filter);
        newpost = (Button)v.findViewById(R.id.newpost);
        listposts = (ListView)v.findViewById(R.id.listofposts);
        listposts.setAdapter(adapter);
        refresh = (Button)v.findViewById(R.id.refresh);

        ButtonHandler myButtonHandler = new ButtonHandler();
        filter.setOnClickListener(myButtonHandler);
        newpost.setOnClickListener(myButtonHandler);
        refresh.setOnClickListener(myButtonHandler);
        return v;
    }

    public class myAdapterPost extends ArrayAdapter<Post> {
        ArrayList<Post> posts;
        public myAdapterPost(ArrayList<Post> posts){
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

            convertView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent newActivity = new Intent(getActivity(), SinglePostActivity.class);
                    newActivity.putExtra("PostIndex", position);
                    startActivity(newActivity);
                }
            });

            return convertView;
        }

    }

    private class ButtonHandler implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.filter:
                    break;
                case R.id.newpost:
                    Intent i = new Intent(getActivity(),MakePost.class);
                    startActivity(i);
                    break;
                case R.id.refresh:
                    networkManager.getPostsForUser(owner.get_id());
                    break;
            }
        }
    }

    public void notifyChange(){
        System.out.println("Called");
        adapter.notifyDataSetChanged();
    }

}
