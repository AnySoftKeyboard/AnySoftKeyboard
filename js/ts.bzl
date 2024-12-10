"""Helpers for testing"""
load("@aspect_rules_ts//ts:defs.bzl", "ts_project")

def ts_library(name, srcs, deps, **kwargs):
    """
    A macro to create a ts_project target.

    Args:
        name: The name of the target to create.
        srcs: The set of sources.
        deps: The needed dependencies.
        **kwargs: To pass to ts_project.
    """

    # https://github.com/aspect-build/rules_ts/blob/main/docs/rules.md#ts_project
    ts_project(
        name = name,
        srcs = srcs,
        out_dir = "dist",
        transpiler = "tsc",
        composite = True,
        tsconfig = "//:tsconfig",
        isolated_typecheck = True,
        deps = deps,
        **kwargs,
    )
