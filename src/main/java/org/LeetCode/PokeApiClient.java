package org.LeetCode;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PokeApiClient {
    private static final String BASE = "https://pokeapi.co/api/v2/pokemon/";
    private final HttpClient http = HttpClient.newHttpClient();
    private final Random rnd = new Random();

    public CompletableFuture<Pokemon> fetchByName(String nameOrId) {
        String url = BASE + nameOrId.toLowerCase(Locale.ROOT);
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).build();

        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() != 200)
                        throw new RuntimeException("Pokémon no encontrado: " + nameOrId);
                    JSONObject json = new JSONObject(resp.body());
                    String name = json.getString("name");

                    // tipos
                    List<String> types = new ArrayList<>();
                    JSONArray jt = json.getJSONArray("types");
                    for (int i = 0; i < jt.length(); i++) {
                        types.add(jt.getJSONObject(i).getJSONObject("type").getString("name"));
                    }

                    // stats: hp, attack, defense, speed
                    JSONArray stats = json.getJSONArray("stats");
                    int hp = findStat(stats, "hp");
                    int attack = findStat(stats, "attack");
                    int defense = findStat(stats, "defense");
                    int speed = findStat(stats, "speed");

                    String sprite = json.getJSONObject("sprites").getString("front_default");

                    return new Pokemon(name, types, hp, attack, defense, speed, sprite);
                });
    }

    public CompletableFuture<Pokemon> fetchRandom() {
        // id aleatorio en un rango seguro; puedes ampliar
        int id = 1 + rnd.nextInt(898);
        return fetchByName(String.valueOf(id));
    }

    private static int findStat(JSONArray stats, String statName) {
        for (int i = 0; i < stats.length(); i++) {
            JSONObject s = stats.getJSONObject(i);
            if (statName.equals(s.getJSONObject("stat").getString("name"))) {
                return s.getInt("base_stat");
            }
        }
        throw new IllegalArgumentException("Stat no encontrada: " + statName);
    }
}

