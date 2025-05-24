# In js/oxc_emitter.bzl

def _oxc_emit_dts_impl(ctx):
    oxc_emitter_tool = ctx.toolchains["@rules_multitool//multitool:toolchain_type"].resolved_tool_path("rust_oxc_dts_emit")

    output_files = []
    tool_args = []
    input_files = []

    for src_label in ctx.attr.srcs:
        for src_file in src_label.files: # A label_list can have multiple files per label (e.g. if it's a filegroup)
            input_files.append(src_file)
            
            # Determine output path: sibling to the input, with .d.ts extension
            # Example: src_file.path = "path/to/file.ts" -> "path/to/file.d.ts"
            #          src_file.short_path = "file.ts" (if under the package path)
            #          src_file.basename = "file.ts"
            #          src_file.extension = "ts"
            
            # base_path_without_ext = src_file.path[:-len(src_file.extension) -1] # remove .ts or .tsx
            # output_dts_path = base_path_without_ext + ".d.ts"
            
            # If input is 'foo/bar.ts', output is 'foo/bar.d.ts' in the bazel-out/package_path/ directory
            # We need to declare the output file relative to the current package's exec_path
            # For files in the current package, their path is 'package_name/src_file.short_path'
            # For files from other packages, their path includes their full repository and package path.
            # ctx.actions.declare_file needs a path unique within the current rule's scope, typically relative to package path.
            
            # Let's use short_path for declaring files within the current package context if possible,
            # or a unique name if files come from different paths to avoid collisions.
            # For simplicity and correctness with cross-package sources, use the full path for uniqueness in naming declared files,
            # then ensure they are placed correctly.
            # The CLI tool takes full paths anyway.

            # The output file path passed to declare_file should be relative to the package's root in bazel-out.
            # src_file.short_path gives 'my_package/my_file.ts' -> 'my_file.ts'
            # If src_file.path is 'fully/qualified/path/to/src/my_package/file.ts'
            # and package is 'my_package', then src_file.short_path is 'file.ts'
            # output_short_path = src_file.short_path[:-len(src_file.extension) -1] + ".d.ts"
            
            # A reliable way to get output path for declare_file:
            # If input is path/to/file.ts, output should be path/to/file.d.ts
            # The tool will write to this absolute path in execroot.
            # declare_file needs a path relative to the package's output directory.
            # Example: if src is `foo.ts` in package `bar`, its path is `bar/foo.ts`.
            # Output `bar/foo.d.ts`. `declare_file("foo.d.ts", sibling = src_file)` is not available.
            
            # The `rust_oxc_dts_emit` tool takes absolute paths for input and output.
            # `ctx.actions.declare_file` declares a file that this action will generate.
            # The path for `declare_file` should be unique for this action.
            # If `src_file.path` is `path/to/package/src.ts`, then `src_file.short_path` is `src.ts`.
            # The output file will be `path/to/package/src.d.ts`.
            # We declare it as `src.d.ts` if it's in the same package, or more complex if not.

            # Let's simplify: assume sources are within the current package or are handled correctly by `src_file.path`.
            # The simplest for `declare_file` is often `src_file.short_path` modified.
            
            output_dts_short_path = src_file.short_path
            if output_dts_short_path.endswith(".ts"):
                output_dts_short_path = output_dts_short_path[:-3] + ".d.ts"
            elif output_dts_short_path.endswith(".tsx"):
                output_dts_short_path = output_dts_short_path[:-4] + ".d.ts"
            else:
                fail("Source file {} is not a .ts or .tsx file".format(src_file.path))

            declared_output_file = ctx.actions.declare_file(output_dts_short_path)
            output_files.append(declared_output_file)
            
            # The CLI tool needs absolute paths (or paths relative to exec root).
            # src_file.path is the path relative to the exec root.
            # declared_output_file.path is also relative to the exec root.
            tool_args.append("{}:{}".format(src_file.path, declared_output_file.path))

    if not input_files:
        fail("No source files provided to oxc_emit_dts rule.")

    action_label = ctx.label.name
    ctx.actions.run(
        executable = oxc_emitter_tool,
        inputs = input_files,
        outputs = output_files,
        arguments = ["--"] + tool_args, # Based on the example: oxc_dts_emit -- [/path/in:/path/out]
        mnemonic = "OxcEmitDts",
        progress_message = "Generating .d.ts for {} using oxc_dts_emit".format(action_label),
    )

    return [DefaultInfo(files = depset(output_files))]

# Rule definition (should remain the same)
oxc_emit_dts = rule(
    implementation = _oxc_emit_dts_impl,
    attrs = {
        "srcs": attr.label_list(
            allow_files = [".ts", ".tsx"],
            mandatory = True,
            doc = "A list of TypeScript (.ts or .tsx) source files.",
        ),
    },
    toolchains = ["@rules_multitool//multitool:toolchain_type"],
    doc = "Generates .d.ts files from TypeScript sources using rust_oxc_dts_emit.",
)
