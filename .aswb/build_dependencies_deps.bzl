"""Loads and re-exports dependencies of build_dependencies.bzl to support different versions of bazel"""

load(
    "@bazel_tools//tools/build_defs/cc:action_names.bzl",
    _CPP_COMPILE_ACTION_NAME = "CPP_COMPILE_ACTION_NAME",
    _C_COMPILE_ACTION_NAME = "C_COMPILE_ACTION_NAME",
)

ZIP_TOOL_LABEL = "@bazel_tools//tools/zip:zipper"

# JAVA

def _get_java_info(target, rule):
    if not JavaInfo in target:
        return None
    p = target[JavaInfo]
    return struct(
        compile_jars = p.compile_jars,
        transitive_compile_time_jars = p.transitive_compile_time_jars,
        java_outputs = p.java_outputs,
    )

IDE_JAVA = struct(
    srcs_attributes = ["java_srcs", "java_test_srcs"],
    get_java_info = _get_java_info,
)

# KOTLIN

def _get_dependency_attribute(rule, attr):
    if hasattr(rule.attr, attr):
        to_add = getattr(rule.attr, attr)
        if type(to_add) == "list":
            return [t for t in to_add if type(t) == "Target"]
        elif type(to_add) == "Target":
            return [to_add]
    return []

def _get_followed_kotlin_dependencies(rule):
    deps = []
    if rule.kind in ["kt_jvm_library_helper", "kt_android_library", "android_library"]:
        deps.extend(_get_dependency_attribute(rule, "_toolchain"))
    if rule.kind in ["kt_jvm_toolchain"]:
        deps.extend(_get_dependency_attribute(rule, "kotlin_libs"))
    return deps

IDE_KOTLIN = struct(
    srcs_attributes = [
        "kotlin_srcs",
        "kotlin_test_srcs",
        "common_srcs",
    ],
    follow_attributes = [],
    follow_additional_attributes = [
        "_toolchain",
        "kotlin_libs",
    ],
    followed_dependencies = _get_followed_kotlin_dependencies,
    toolchains_aspects = [],
)

# PROTO

def _get_java_proto_info(target, rule):
    return None

def _get_followed_java_proto_dependencies(rule):
    deps = []
    if rule.kind in ["proto_lang_toolchain", "java_rpc_toolchain"]:
        deps.extend(_get_dependency_attribute(rule, "runtime"))
    if rule.kind in ["_java_grpc_library", "_java_lite_grpc_library"]:
        deps.extend(_get_dependency_attribute(rule, "_toolchain"))
    return deps

IDE_JAVA_PROTO = struct(
    get_java_proto_info = _get_java_proto_info,
    srcs_attributes = [],
    follow_attributes = ["_toolchain", "runtime"],
    followed_dependencies = _get_followed_java_proto_dependencies,
    toolchains_aspects = [],
)

# CC

def _get_cc_toolchain_target(rule):
    if hasattr(rule.attr, "_cc_toolchain"):
        return getattr(rule.attr, "_cc_toolchain")
    return None

IDE_CC = struct(
    c_compile_action_name = _C_COMPILE_ACTION_NAME,
    cpp_compile_action_name = _CPP_COMPILE_ACTION_NAME,
    follow_attributes = ["_cc_toolchain"],
    toolchains_aspects = [],
    toolchain_target = _get_cc_toolchain_target,
)
