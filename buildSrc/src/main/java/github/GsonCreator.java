package github;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class GsonCreator {
  public static Gson create() {
    return new GsonBuilder().create();
  }
}
