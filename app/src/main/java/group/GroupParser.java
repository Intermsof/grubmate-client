package group;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

    public GroupParser(){}

    public GroupParser(GroupViewFragment gvf) {
        this.groupViewFragment = gvf;
    }

    public boolean getGroupForOwner() {
        try {
            owner = UserSingleton.getUserInstance();

            GetGroupsWithUserID getGroupsWithUserID = new GetGroupsWithUserID(owner.get_id());
            getGroupsWithUserID.execute();

            return true;
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("set group parser failed");
            return false;
        }
    }

    public boolean addGroupForOwner(Group group) {
        try {
            owner = UserSingleton.getUserInstance();

            AddGroupWithUserID addGroupWithUserID = new AddGroupWithUserID(owner.get_id(), group);
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
        private Group group;

        public AddGroupWithUserID(String userid, Group group){
            this.userid = userid;
            this.group = group;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                // This is getting the url from the string we passed in
                URL url = new URL("https://grubmateteam3.herokuapp.com/api/group");
                // Create the urlConnection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                // Send the post body
                JSONObject groupJson = new JSONObject();
                //JSONObject userJson = new JSONObject();
                JSONArray friendsJson = new JSONArray(group.getUsers());

                // making json object
                groupJson.put("name",group.getName());
                groupJson.put("users",friendsJson);

                // debug
                System.out.println("group json: " + groupJson);

                // write to server
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                String jsonString = groupJson.toString();
                writer.write(jsonString);
                System.out.println(jsonString);
                writer.flush();
                writer.close();

                InputStream is = urlConnection.getInputStream();
                //Wrap InputStream with InputStreamReader
                //Input stream of bytes is converted to stream of characters
                //Buffer reading operation to improve efficiency
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                //Read all characters into String data
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("response: " +response.toString());

            } catch (Exception e) {
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

                parseGroupIDs(is);

            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public void parseGroupIDs(InputStream is) throws IOException {
        ArrayList<String> groupList = convertStreamToArray(is);

        // return if empty
        if (groupList==null || groupList.isEmpty()) {
            System.out.println("empty group array");
            return;
        } else {

            try {
                    int len = groupList.size();
                    for (int i = 0; i < len; i++) {
                        // parse each group by id
                        String id = groupList.get(i);
                        System.out.println("groupid: " + id);

                        GetSingleGroupWithGroupID getSingleGroupWithGroupID = new GetSingleGroupWithGroupID(id);
                        getSingleGroupWithGroupID.execute();
                    }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed parsing group ids");
            }
        }
    }


    public void parseSingleGroup(InputStream is) throws IOException {
        String jsonString = convertStreamToString(is);

        // return if empty
        if (jsonString.equalsIgnoreCase("[]")) {
            System.out.println("empty group array");
            return;
        }

        try{
            JSONObject groupObject = new JSONObject(jsonString);
            System.out.println("getting group: " + groupObject);

            // sets group name and friend list
            String groupName = groupObject.getString("name");

            JSONArray jsonFriendArray = groupObject.getJSONArray("users");
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

    private ArrayList<String> convertStreamToArray(InputStream in) throws IOException{
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        ArrayList<String> result = new ArrayList<String>();
        reader.beginArray();
        while (reader.hasNext()) {
            String x = reader.nextString();
            result.add(x);
        }
        reader.endArray();
        return result;
    }

    private class GetSingleGroupWithGroupID extends AsyncTask<String, Void, Void> {
        private String groupid;
        public GetSingleGroupWithGroupID(String groupid){
            this.groupid = groupid;
        }

        @Override
        protected Void doInBackground(String... params) {
            try{
                URL url = new URL("https://grubmateteam3.herokuapp.com/api/singlegroup?groupid="+groupid);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();

                parseSingleGroup(is);

            } catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

}

