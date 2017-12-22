/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myapp;

import com.google.cloud.dialogflow.v2beta1.Intent;
import com.google.cloud.dialogflow.v2beta1.IntentsClient;
import com.google.cloud.dialogflow.v2beta1.ProjectAgentName;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class IntentOne extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    try {
      List<Intent.TrainingPhrase> trainingPhrases = new ArrayList<>();

      Intent.TrainingPhrase.Part part1 = Intent.TrainingPhrase.Part.newBuilder().setEntityType("@check")
              .setText("超声").setAlias("超声").setUserDefined(true).build();

      Intent.TrainingPhrase.Part part2 = Intent.TrainingPhrase.Part.newBuilder()
              .setText("检查能报销不").build();
      trainingPhrases.add(Intent.TrainingPhrase.newBuilder().addParts(part1).addParts(part2).build());

      part1 = Intent.TrainingPhrase.Part.newBuilder().setEntityType("@check").setAlias("胃镜").setUserDefined(true)
              .setText("胃镜").build();

      part2 = Intent.TrainingPhrase.Part.newBuilder()
              .setText("检查能报销不").build();
      trainingPhrases.add(Intent.TrainingPhrase.newBuilder().addParts(part1).addParts(part2).build());

      Intent intent = Intent.newBuilder()
              .setDisplayName("testIntent")
              .addAllTrainingPhrases(trainingPhrases)
              .setMlEnabled(true)
              .build();
      try (IntentsClient intentsClient = IntentsClient.create()) {
        Intent intent1 = intentsClient.createIntent(ProjectAgentName.of("test-1b8b7"),intent);
        System.out.println(intent1);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
