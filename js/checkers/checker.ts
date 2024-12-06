export interface Checker {
    get name(): string;
    check(root_dir: string, dir_skip_predicate: (dirname: string)=>boolean): Promise<void>;
}