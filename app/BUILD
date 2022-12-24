android_library(
    name = "basic_lib",
    custom_package = "com.example.mediapipe.edgedetection",
    srcs = ["java/com/example/mediapipe/edgedetection/MainActivity.java"],
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = [
        "//third_party:androidx_constraint_layout",
        "//third_party:androidx_appcompat",
        "//third_party:opencv",
        "//mediapipe/java/com/google/mediapipe/components:android_components",
        "//mediapipe/java/com/google/mediapipe/components:android_camerax_helper",
        "//mediapipe/java/com/google/mediapipe/glutil",
        "//mediapipe/java/com/google/mediapipe/framework:android_framework",
    ],
)

cc_binary(
    name = "libmediapipe_jni.so",
    linkshared = 1,
    linkstatic = 1,
    deps = [
        "//mediapipe/java/com/google/mediapipe/framework/jni:mediapipe_framework_jni",
        "//mediapipe/graphs/edge_detection:mobile_calculators",
    ],
)

cc_library(
    name = "mediapipe_jni_lib",
    srcs = [":libmediapipe_jni.so"],
    alwayslink = 1,
)

android_binary(
    name = "edgedetection",
    assets = [
        "//mediapipe/graphs/edge_detection:mobile_gpu_binary_graph",
    ],
    assets_dir = "",
    custom_package = "com.example.mediapipe.edgedetection",
    manifest = "AndroidManifest.xml",
    manifest_values = {
        "applicationId": "com.example.mediapipe.edgedetection",
        "appName": "MediaPipe Edge Detection",
        "mainActivity": ".MainActivity",
        "cameraFacingFront": "False",
        "binaryGraphName": "mobile_gpu_binary_graph",
        "inputVideoStreamName": "input_video",
        "outputVideoStreamName": "output_video",
    },
    multidex = "native",
    deps = [
        ":basic_lib",
        ":mediapipe_jni_lib",
    ],
)