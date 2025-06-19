export type PromotionStepGetter = (currentEpoch: number, headCommitEpoch: number) => number;
class DeploymentSteps {
  public readonly environmentSteps: string[];
  public readonly getStepIndex: PromotionStepGetter;

  constructor(environmentSteps: string[], getStepIndex: PromotionStepGetter) {
    this.environmentSteps = [...environmentSteps];
    this.getStepIndex = getStepIndex;
  }
}
export class DeploymentConfiguration extends DeploymentSteps {
  public readonly name: string;

  constructor(name: string, environmentSteps: string[], getStepIndex: PromotionStepGetter) {
    super(environmentSteps, getStepIndex);
    this.name = name;
  }
}

// 3 represents Wednesday (0 is Sunday)
export const promoteOnWednesday: PromotionStepGetter = (currentEpoch: number, _: number) => {
  const current = new Date(currentEpoch);
  return current.getDay() === 3 ? 1 : 0;
};

export const promoteByDay: PromotionStepGetter = (currentEpoch: number, headCommitEpoch: number) => {
  const diffInDays = Math.floor((currentEpoch - headCommitEpoch) / (1000 * 60 * 60 * 24));
  return diffInDays;
};

/**
 * the key is the environment name
 * the value is the name of the environment to by day since the last commit date.
 *
 * For example:
 * for imeProduction:
 * - on day 0 (deploy): production_010 (production channel, 10% rollout)
 * - on day 1 (migrate): production_020 (production channel, 20% rollout)
 * - on day 2: '' means no-op
 */
const deploymentConfigurations: Record<string, DeploymentSteps> = {
  imeMain: new DeploymentSteps(['alpha_100', 'beta_100'], promoteOnWednesday),
  imeProduction: new DeploymentSteps(
    [
      'production_010',
      '',
      'production_020',
      '',
      '',
      'production_030',
      'production_040',
      'production_050',
      '',
      '',
      '',
      'production_075',
      '',
      'production_100',
    ],
    promoteByDay,
  ),
  addOnsMain: new DeploymentSteps(['alpha_100', 'beta_100'], promoteOnWednesday),
  addOnsProduction: new DeploymentSteps(['production_010', '', 'production_050', '', 'production_100'], promoteByDay),
};

export type DeploymentNameType = keyof typeof deploymentConfigurations;

export const getDeploymentConfiguration = (deploymentName: DeploymentNameType): DeploymentConfiguration => {
  const steps = deploymentConfigurations[deploymentName];
  return new DeploymentConfiguration(deploymentName, steps.environmentSteps, steps.getStepIndex);
};

export const calculateDeploymentName = (refname: string, shardName: string): DeploymentNameType => {
  if (refname === 'main') {
    if (shardName === 'ime') {
      return 'imeMain';
    } else {
      return 'addOnsMain';
    }
  } else {
    if (shardName === 'ime') {
      return 'imeProduction';
    } else {
      return 'addOnsProduction';
    }
  }
};
