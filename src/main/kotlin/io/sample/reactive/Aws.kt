package io.sample.reactive

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.codeguruprofilerjavaagent.Profiler
import software.amazon.awssdk.regions.Region

val provider: AwsCredentialsProvider = AwsCredentialsProvider {
    ProfileCredentialsProvider.create("snozaki-private").resolveCredentials()
}

/**
 * AWS CodeGuruのプロファイラを追加する
 */
fun setProfiler() {
    Profiler.builder()
        .profilingGroupName("general-profiler")
        .awsRegionToReportTo(Region.AP_NORTHEAST_1)
        .awsCredentialsProvider(provider)
        .withHeapSummary(true)
        .build()
        .start()
}
