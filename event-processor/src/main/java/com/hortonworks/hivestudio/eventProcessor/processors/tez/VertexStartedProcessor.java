/*
 *
 * HORTONWORKS DATAPLANE SERVICE AND ITS CONSTITUENT SERVICES
 *
 * (c) 2016-2018 Hortonworks, Inc. All rights reserved.
 *
 * This code is provided to you pursuant to your written agreement with Hortonworks, which may be the terms of the
 * Affero General Public License version 3 (AGPLv3), or pursuant to a written agreement with a third party authorized
 * to distribute this code.  If you do not have a written agreement with Hortonworks or with an authorized and
 * properly licensed third party, you do not have any rights to this code.
 *
 * If this code is provided to you under the terms of the AGPLv3:
 * (A) HORTONWORKS PROVIDES THIS CODE TO YOU WITHOUT WARRANTIES OF ANY KIND;
 * (B) HORTONWORKS DISCLAIMS ANY AND ALL EXPRESS AND IMPLIED WARRANTIES WITH RESPECT TO THIS CODE, INCLUDING BUT NOT
 *   LIMITED TO IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE;
 * (C) HORTONWORKS IS NOT LIABLE TO YOU, AND WILL NOT DEFEND, INDEMNIFY, OR HOLD YOU HARMLESS FOR ANY CLAIMS ARISING
 *   FROM OR RELATED TO THE CODE; AND
 * (D) WITH RESPECT TO YOUR EXERCISE OF ANY RIGHTS GRANTED TO YOU FOR THE CODE, HORTONWORKS IS NOT LIABLE FOR ANY
 *   DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES INCLUDING, BUT NOT LIMITED TO,
 *   DAMAGES RELATED TO LOST REVENUE, LOST PROFITS, LOSS OF INCOME, LOSS OF BUSINESS ADVANTAGE OR UNAVAILABILITY,
 *   OR LOSS OR CORRUPTION OF DATA.
 *
 */
package com.hortonworks.hivestudio.eventProcessor.processors.tez;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.hadoop.fs.Path;
import org.apache.tez.common.ATSConstants;

import com.hortonworks.hivestudio.common.entities.VertexInfo;
import com.hortonworks.hivestudio.common.repository.transaction.DASTransaction;
import com.hortonworks.hivestudio.eventProcessor.processors.ProcessingStatus;
import com.hortonworks.hivestudio.eventProcessor.processors.TezEventProcessor;
import com.hortonworks.hivestudio.eventProcessor.processors.TezEventType;
import com.hortonworks.hivestudio.eventdefs.TezHSEvent;
import com.hortonworks.hivestudio.query.entities.repositories.VertexInfoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertexStartedProcessor extends TezEventProcessor {

  private final Provider<VertexInfoRepository> vertexInfoRepositoryProvider;
  @Inject
  public VertexStartedProcessor(Provider<VertexInfoRepository> vertexInfoRepositoryProvider) {
    this.vertexInfoRepositoryProvider = vertexInfoRepositoryProvider;
  }

  @Override
  @DASTransaction
  protected ProcessingStatus processValidEvent(TezHSEvent event, Path filePath) {
    VertexInfoRepository vertexInfoRepository = vertexInfoRepositoryProvider.get();
    Collection<VertexInfo> allByDagId = vertexInfoRepository.findAllByDagId(event.getDagId());

    Map<String, String> otherInfo = event.getOtherInfo();
    String vertexId = event.getVertexId();
    Optional<VertexInfo> vertexInfoOptional = allByDagId.stream()
        .filter(x -> x.getVertexId().equals(vertexId))
        .findFirst();
    if (vertexInfoOptional.isPresent()) {
      VertexInfo vertexInfo = vertexInfoOptional.get();
      vertexInfo.setStartRequestedTime(
          Long.valueOf(otherInfo.get(ATSConstants.START_REQUESTED_TIME)));
      vertexInfo.setStatus(otherInfo.get(ATSConstants.STATUS));
      vertexInfoRepository.save(vertexInfo);
    } else {
      log.error("Failed to get process event of type {}, Vertex info not found for id {}",
          event.getEventType(), vertexId);
      return new ProcessingStatus(ProcessingStatus.Status.ERROR,
          Optional.of(new RuntimeException("Vertex info not found for id " + vertexId)));
    }
    return new ProcessingStatus(ProcessingStatus.Status.SUCCESS, Optional.empty());
  }

  @Override
  protected TezEventType[] validEvents() {
    return new TezEventType[]{TezEventType.VERTEX_STARTED};
  }
}
