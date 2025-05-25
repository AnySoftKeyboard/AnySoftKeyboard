"""Bazel rule for generating .d.ts files from TypeScript sources using rust_oxc_dts_emit."""

def _oxc_emit_dts_impl(ctx):
    output_files = []
    tool_args = []
    input_files = []

    for src_file in ctx.files.srcs:
        input_files.append(src_file)

        # Replacing the extension with "d.ts"
        output_dts_base_name = "{}d.ts".format(src_file.basename[:-(len(src_file.extension))])
        declared_output_file = ctx.actions.declare_file(output_dts_base_name, sibling = src_file)
        output_files.append(declared_output_file)

        # The CLI tool needs absolute paths (or paths relative to exec root).
        # src_file.path is the path relative to the exec root.
        # declared_output_file.path is also relative to the exec root.
        tool_args.append("{}:{}".format(src_file.path, declared_output_file.path))

    if not input_files:
        fail("No source files provided to oxc_emit_dts rule.")

    oxc_emitter_tool = ctx.toolchains["@multitool//tools/rust_oxc_dts_emit:toolchain_type"].executable

    ctx.actions.run(
        executable = oxc_emitter_tool,
        inputs = input_files,
        outputs = output_files,
        arguments = tool_args,
        mnemonic = "OxcEmitDts",
        progress_message = "Generating .d.ts for {} using oxc_dts_emit".format(ctx.label.name),
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
    toolchains = ["@multitool//tools/rust_oxc_dts_emit:toolchain_type"],
    doc = "Generates .d.ts files from TypeScript sources using rust_oxc_dts_emit.",
)
