package com.runewatchsa;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class CaseManager
{
    private final Map<String, Case> cases = new ConcurrentHashMap<>();
    private final OkHttpClient client;
    private final Gson gson;

    @Setter
    private Consumer<Map<String, Case>> onDataLoaded;

    @Inject
    public CaseManager(OkHttpClient client, Gson gson)
    {
        this.client = client;
        this.gson = gson;
    }

    public void refresh()
    {
        final String URL = "https://runewatch-sa.onrender.com/api/cases";
        
        Request request = new Request.Builder()
            .url(URL)
            .build();

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.error("Failed to fetch RuneWatch SA cases", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    JsonArray array = gson.fromJson(new InputStreamReader(response.body().byteStream()), JsonArray.class);

                    cases.clear();
                    for (JsonElement element : array)
                    {
                        if (!element.isJsonObject()) continue;
                        JsonObject data = element.getAsJsonObject();
                        Case c = gson.fromJson(data, Case.class);

                        // Nome atual
                        String currentName = Text.standardize(c.getName());
                        cases.put(currentName, c);

                        // Histórico de nomes
                        if (data.has("nameHistory") && data.get("nameHistory").isJsonArray())
                        {
                            JsonArray history = data.getAsJsonArray("nameHistory");
                            for (JsonElement hElement : history)
                            {
                                String oldName = Text.standardize(hElement.getAsString());
                                if (!oldName.isEmpty() && !cases.containsKey(oldName))
                                {
                                    cases.put(oldName, c);
                                }
                            }
                        }
                    }

                    log.info("Loaded {} index entries for {} RuneWatch SA cases", cases.size(), array.size());
                    
                    if (onDataLoaded != null)
                    {
                        onDataLoaded.accept(cases);
                    }
                }
                response.close();
            }
        });
    }

    public Case get(String name)
    {
        return cases.get(Text.standardize(name));
    }

    public Map<String, Case> getCases()
    {
        return cases;
    }
}
