load(":build_dependencies.bzl", _collect_dependencies = "collect_dependencies", _package_dependencies = "package_dependencies")

_config = struct(
    include = [
        "//",
    ],
    exclude = [
        "//bazel-bin",
        "//bazel-genfiles",
        "//bazel-out",
        "//bazel-testlogs",
        "//bazel-AnySoftKeyboard",
        "//.aswb",
    ],
    always_build_rules = [
        "_java_grpc_library",
        "_java_lite_grpc_library",
        "aar_import",
        "af_internal_soyinfo_generator",
        "java_import",
        "java_lite_proto_library",
        "java_mutable_proto_library",
        "java_proto_library",
        "java_stubby_library",
        "kt_grpc_library_helper",
        "kt_proto_library_helper",
        "kt_stubby_library_helper",
    ],
    generate_aidl_classes = True,
    use_generated_srcjars = False,
    experiment_multi_info_file = True,
)

collect_dependencies = _collect_dependencies(_config)
package_dependencies = _package_dependencies(_config)
