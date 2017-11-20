package group;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import objects.Group;
import objects.Notification;
import objects.Post;
import objects.User;
import objects.UserSingleton;


public class GroupParser {
    private UserSingleton owner;
    private GroupViewFragment groupViewFragment;

    GroupParser(){}

    GroupParser(GroupViewFragment gvf) {
        this.groupViewFragment = gvf;
    }

    public boolean getGroupForOwner() {
        try {
            owner = UserSingleton.getUserInstance();

            GetGroupsWithUserID getGroupWithUserID = new GetGroupsWithUserID(owner.get_id());
            getGroupWithUserID.execute();

            return true;
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("set group parser failed");
            return false;
        }
    }

    public boolean addGroupForOwner() {
        try {
            owner = UserSingleton.getUserInstance();

            AddGroupWithUserID addGroupWithUserID = new AddGroupWithUserID(owner.get_id());
            addGroupWithUserID.execute();

            return true;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("add group parser failed");
            return false;
        }
    }

    private class AddGroupWithUserID extends AsyncTask<String, Void, Void> {
        private String userid;
        public AddGroupWithUserID(String userid){
            this.userid = userid;
        }

        @Override
        protected Void doInBackground(String... params) {
            try{
                URL url = new URL("https://grubmateteam3.herokuapp.com/api/group?userid="+userid);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();

                parseGroups(is);

            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private class GetGroupsWithUserID extends AsyncTask<String, Void, Void> {
        private String userid;
        public GetGroupsWithUserID(String userid){
            this.userid = userid;
        }

        @Override
        protected Void doInBackground(String... params) {
            try{
                URL url = new URL("https://grubmateteam3.herokuapp.com/api/group?userid="+userid);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();

                parseGroups(is);

            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public void parseGroups(InputStream is) throws IOException {
        String jsonString = convertStreamToString(is);

        // return if empty
        if (jsonString.equalsIgnoreCase("[]")) {return;}

        try{
            JSONObject groupObject = new JSONObject(jsonString);

            // sets group name and friend list
            String groupName = groupObject.getString("name");

            JSONArray jsonFriendArray = groupObject.getJSONArray("friends");
            ArrayList<String> friendList = new ArrayList<String>();
            if (jsonFriendArray != null) {
                int len = jsonFriendArray.length();
                for (int i=0;i<len;i++){
                    // gets friends ids
                    String friendID = jsonFriendArray.get(i).toString();
                    String friendName = owner.getFriendNameByID(friendID);

                    // add friend to list
                    friendList.add(friendName);
                }

                // create and add new group
                Group group = new Group(groupName, friendList);
                owner.addGroup(group);

            } else {
                System.out.println("group parser null");
            }

        }catch (JSONException e){
            e.printStackTrace();
            System.out.println("failed parsing friends under groups");
        }
    }

    private String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}

