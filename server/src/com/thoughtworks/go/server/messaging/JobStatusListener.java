/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.server.messaging;

import com.thoughtworks.go.domain.Stage;
import com.thoughtworks.go.domain.StageResult;
import com.thoughtworks.go.domain.StageState;
import com.thoughtworks.go.server.domain.StageStatusHandler;
import com.thoughtworks.go.server.service.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobStatusListener implements GoMessageListener<JobStatusMessage> {
    private final JobStatusTopic jobStatusTopic;
    private StageService stageService;
    private final StageStatusTopic stageStatusTopic;

    @Autowired
    public JobStatusListener(JobStatusTopic jobStatusTopic, StageService stageService,
                             StageStatusTopic stageStatusTopic) {
        this.jobStatusTopic = jobStatusTopic;
        this.stageService = stageService;
        this.stageStatusTopic = stageStatusTopic;
    }

    public void init() {
        jobStatusTopic.addListener(this);
    }

    public void onMessage(final JobStatusMessage message) {
        if (message.getJobState().isCompleted()) {
            final Stage stage = stageService.findStageWithIdentifier(message.getStageIdentifier());
            stage.statusHandling(new StageStatusHandler() {
                public void onNormalCompletion(StageState stageState, StageResult stageResult) {
                    stageStatusTopic.post(new StageStatusMessage(message.getStageIdentifier(), stageState, stageResult));
                }
            });
        }
    }


}
