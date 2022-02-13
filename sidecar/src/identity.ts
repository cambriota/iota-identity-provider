import {
  Client,
  Config,
  Network,
  VerifierOptions,
} from "@iota/identity-wasm/node";

const config = Config.fromNetwork(Network.mainnet());
const client = Client.fromConfig(config);

export const check = async (presentation: any) => {
  console.log("Checking credential ...");
  console.log(presentation);
  try {
    const result = await client.checkPresentation(JSON.stringify(presentation), new VerifierOptions({ challenge: presentation.proof.challenge }));
    console.log(`Verification result: ${JSON.stringify(result)}`);
    return result;
  } catch (error) {
    console.warn(error);
    return { error };
  }
};
