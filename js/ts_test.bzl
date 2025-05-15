"""Helpers for testing"""

load("@aspect_rules_js//js:defs.bzl", "js_test")
load(":ts.bzl", "ts_library")

def ts_test(name, entry_point, deps):
    """
    A macro to create a jest-based test.

    Args:
        name: The name of the test target to create.
        entry_point: The test entry-point file.
        deps: The needed dependencies, including the code-under-test.
    """
    test_data = []
    test_data.extend(deps)

    lib_entry_point_name = "{}_entry_point_lib".format(name)
    ts_library(
        name = lib_entry_point_name,
        srcs = [entry_point],
        testonly = True,
        deps = test_data,
    )

    js_test(
        name = name,
        entry_point = lib_entry_point_name,
        data = [lib_entry_point_name] + test_data,
        args = [
            # args passed to process.argv
        ],
        node_options = [
            "--enable-source-maps",
            "--test-force-exit",
        ],
        env = {
            "NODE_PATH": "$(BINDIR):.",
            "FORCE_COLOR": "1",
        },
    )
