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

package com.hortonworks.hivestudio.debugBundler.source;

import com.google.inject.Inject;
import com.hortonworks.hivestudio.debugBundler.framework.Artifact;
import com.hortonworks.hivestudio.debugBundler.framework.ArtifactDownloadException;
import com.hortonworks.hivestudio.debugBundler.framework.ArtifactSource;
import com.hortonworks.hivestudio.debugBundler.framework.HttpArtifact;
import com.hortonworks.hivestudio.debugBundler.framework.Params;
import org.apache.hadoop.conf.Configuration;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SliderInstanceJmx implements ArtifactSource {

  private final Configuration hadoopConf;

  @Inject
  public SliderInstanceJmx(Configuration hadoopConf) {
    this.hadoopConf = hadoopConf;
  }

  @Override
  public boolean hasRequiredParams(Params params) {
    return params.getSliderInstanceUrls() != null;
  }

  @Override
  public List<Artifact> getArtifacts(Params params) {
    List<Artifact> artifacts = new ArrayList<>();
    for (String url : params.getSliderInstanceUrls()) {
      try {
        URIBuilder builder = new URIBuilder(url);
        builder.setPath(builder.getPath() + "/jmx");
        artifacts.add(new HttpArtifact(hadoopConf,
            "SLIDER_AM/" + builder.getHost() + ":" + builder.getPort() + "/jmx",
            builder.build().toString(), false));
      } catch (URISyntaxException e) {
        // Return this to user.
        e.printStackTrace();
      }
    }
    return artifacts;
  }

  @Override
  public void updateParams(Params params, Artifact artifact, Path path)
      throws ArtifactDownloadException {
  }
}
