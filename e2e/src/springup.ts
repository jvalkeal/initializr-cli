// import * as exec from '@actions/exec';
// import { ExecOptions } from '@actions/exec';

// export interface ExecResult {
//   success: boolean;
//   stdout: string;
//   stderr: string;
// }

// export interface SpringUpOptions {
//   command: string;
//   options?: string[];
// }

// export const execSpringUp = async (options: SpringUpOptions, stdin?: Buffer): Promise<ExecResult> => {
//   let args: string[] = [];
//   if (options?.command) {
//     args.push(options.command);
//   }
//   if (options?.options) {
//     options.options.forEach(o => {
//       args.push(o);
//     });
//   }
//   return execSpringUpRaw(args, true, {}, stdin);
// };

// export const execSpringUpRaw = async (
//   args: string[] = [],
//   silent?: boolean,
//   env?: { [key: string]: string },
//   stdin?: Buffer
// ): Promise<ExecResult> => {
//   let stdout: string = '';
//   let stderr: string = '';

//   const options: ExecOptions = {
//     silent: silent,
//     ignoreReturnCode: true
//   };
//   if (env) {
//     options.env = env;
//   }
//   if (stdin) {
//     options.input = stdin;
//   }
//   options.listeners = {
//     stdout: (data: Buffer) => {
//       const d = data.toString();
//       console.log('sss1', d);
//       stdout += d;
//     },
//     stderr: (data: Buffer) => {
//       console.log('sss2', data.toString());
//       stderr += data.toString();
//     }
//   };

//   // exec.getExecOutput('spring-up', args, options);
//   const returnCode: number = await exec.exec('spring-up', args, options);

//   return {
//     success: returnCode === 0,
//     stdout: stdout.trim(),
//     stderr: stderr.trim()
//   };
// };
