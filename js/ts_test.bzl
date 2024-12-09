"""Helpers for testing"""
load("@aspect_rules_ts//ts:defs.bzl", "ts_project")
load("@aspect_rules_jest//jest:defs.bzl", "jest_test")

def ts_jest_test(name, srcs, deps):
    """
    A macro to create a jest-based test.

    Args:
        name: The name of the test target to create.
        srcs: The set of sources for the test code.
        deps: The needed dependencies, including the code-under-test.
    """
    lib_target_name = "{}_lib".format(name)
    test_data = [
        "//:node_modules/@types/jest",
        "//:node_modules/ts-jest",
        "//:node_modules/babel-jest",
        "//:node_modules/@jest/globals",
    ]
    test_data.extend(deps)

    ts_project(
        name = lib_target_name,
        srcs = srcs,
        testonly = True,
        transpiler = "tsc",
        composite = True,
        tsconfig = "//js:tsconfig",
        isolated_typecheck = True,
        deps = test_data,
    )

    jest_test(
        name = name,
        config = "//js:jest.config",
        data = [lib_target_name] + test_data,
        node_modules = "//:node_modules",
        node_options = [
            "--experimental-vm-modules",
        ],
    )