package app;

import app.controllers.ControllerMain;
import app.controllers.ControllerUI;
import com.google.gson.Gson;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;


/**
 * Route list
 */
public final class Main
{
    static Gson gson = new Gson();

    public static void main(final String[] args) {

        new Thread (() -> {
            try {
                ControllerMain.runDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        staticFileLocation("/resources"); // Static files

        get("/", (request, response) -> {
            response.redirect("/index");
            return "redirected";
        });

        get("/index", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            try {
                model.put("dates", ControllerUI.getResultList());
            } catch (Exception ex) {
                model.put("error", ex.getMessage());
            }
            return new ModelAndView(model, "vm/hello.vm");
        }, new VelocityTemplateEngine());

        get("/results", (request, response)
                -> ControllerUI.getResultList(), gson::toJson);

        get("/results/:date", (request, response)
                -> ControllerUI.getResult(request.params(":date")), gson::toJson );

        get("/results/tags/:name", (request, response)
                -> ControllerUI.getResultByTag(request.params(":name")), gson::toJson );

        get("/results/:date/details", (request, response)
                -> ControllerUI.getSummary(request.params(":date")), gson::toJson );

        post("/results", (request, response)
                -> ControllerMain.score(), gson::toJson);

        post("/results/:date/:windowSize", (request, response)
                -> ControllerMain.initialize(request.params(":date"), Integer.parseInt(request.params(":windowSize"))), gson::toJson);

        delete("/results", (request, response)
                -> ControllerMain.deleteResult(), gson::toJson);
    }
}