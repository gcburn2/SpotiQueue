package com.AJKH.SpotiQueue.Spotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.AJKH.SpotiQueue.Preferences;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SpotifyHttpUtil {
    private Context appContext;
    private String spotifyBaseUrl = "https://api.spotify.com/v1";

    public SpotifyHttpUtil(Context appContext) {
        this.appContext = appContext;
    }

    public void addTrackToPlaylist(String trackId) {
        final String spotifyAuthToken = getAuthToken();
        String spotifyUsername = getUsername();
        String searchUrl = spotifyBaseUrl + "/users/" + spotifyUsername + "/playlists/" + getPlaylistId() + "/tracks?uris=spotify%3Atrack%3A" + trackId;
        RequestQueue queue = Volley.newRequestQueue(appContext);

        StringRequest postRequest = new StringRequest(Request.Method.POST, searchUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + spotifyAuthToken);
                params.put("Accept", "application/jason");

                return params;
            }
        };
        queue.add(postRequest);
    }

    public void searchSpotifyTrack(String songString) {
        songString = songString.replaceAll("\\s", "%20");
        String searchUrl = spotifyBaseUrl + "/search?q=" + "track:" + songString + "&type=track";
        RequestQueue queue = Volley.newRequestQueue(appContext);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, searchUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        InputStream spotifyResponse = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
                        try {
                            String trackId = new SpotifyResponseParser().readSpotifyJsonResponse(spotifyResponse);
                            if (trackId != null) {
                                addTrackToPlaylist(trackId);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);
    }

    public void searchSpotifyTrack(String songString, String artistString) {
        songString = songString.replaceAll("\\s", "%20");
        artistString = artistString.replaceAll("\\s", "%20");
        String searchUrl = spotifyBaseUrl + "/search?q=" + "artist:" + artistString + "%20track:" + songString + "&type=track";
        RequestQueue queue = Volley.newRequestQueue(appContext);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, searchUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        InputStream spotifyResponse = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
                        try {
                            String trackId = new SpotifyResponseParser().readSpotifyJsonResponse(spotifyResponse);
                            if (trackId != null) {
                                addTrackToPlaylist(trackId);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(stringRequest);
    }

    public void createSpotifyPlaylist() {
        final String spotifyAuthToken = getAuthToken();
        String spotifyUsername = getUsername();
        String url = spotifyBaseUrl + "/users/" + spotifyUsername + "/playlists";

        final JSONObject jsonBody;
        try {
            jsonBody = new JSONObject("{\"name\":\"SpotiQueuePlaylist\", \"public\":false}");
            RequestQueue queue = Volley.newRequestQueue(appContext);
            JsonObjectRequest postRequest = new JsonObjectRequest(url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.print(response);
                            try {
                                SharedPreferences.Editor editor = appContext.getSharedPreferences(Preferences.PROPERTIES, appContext.MODE_PRIVATE).edit();
                                editor.putString(Preferences.SPOTIFY_PLAYLIST_ID, response.getString("id"));
                                editor.commit();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("ERROR", "error => " + error.toString());
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + spotifyAuthToken);
                    params.put("Accept", "application/jason");

                    return params;
                }
            };
            queue.add(postRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getAuthToken() {
        SharedPreferences prefs = appContext.getSharedPreferences(Preferences.PROPERTIES, appContext.MODE_PRIVATE);
        return prefs.getString(Preferences.SPOTIFY_AUTH_TOKEN, null);
    }

    public String getUsername() {
        SharedPreferences prefs = appContext.getSharedPreferences(Preferences.PROPERTIES, appContext.MODE_PRIVATE);
        return prefs.getString(Preferences.SPOTIFY_USERNAME, null);
    }

    public String getPlaylistId() {
        SharedPreferences prefs = appContext.getSharedPreferences(Preferences.PROPERTIES, appContext.MODE_PRIVATE);
        return prefs.getString(Preferences.SPOTIFY_PLAYLIST_ID, null);
    }
}
