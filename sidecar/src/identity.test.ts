import { check } from "./identity";

describe("Identity tests", () => {
  it("can verify a given credential", async () => {
    const verifiablePresentation = {
      "@context": "https://www.w3.org/2018/credentials/v1",
      id: "foo:bar:presentationid29381738",
      type: ["VerifiablePresentation", "LoginWithIOTAIdentity"],
      verifiableCredential: {
        "@context": "https://www.w3.org/2018/credentials/v1",
        id: "https://example.org/credentials/9ffa87ed-0f18-2843-c729-69a4b027d243",
        type: ["VerifiableCredential", "Login"],
        credentialSubject: {
          did: "did:iota:GJo2HnpjhR75Ty25v38fZ3LCTyXU5gux8Prd9KYmus4N",
          email: "john.doe@example.org",
          firstName: "John",
          lastName: "Doe",
          username: "johndoe",
        },
        issuer: "did:iota:GJo2HnpjhR75Ty25v38fZ3LCTyXU5gux8Prd9KYmus4N",
        issuanceDate: "2021-11-15T13:38:20Z",
        proof: {
          type: "JcsEd25519Signature2020",
          verificationMethod: "#authentication",
          signatureValue:
            "2K1FBxx1xBwyazdMvhFaxFY9sZPMri9w8ycxqQ4SXaFxY89yHr7JuA4cYXzqBvkDjhqW41gQFVyiv4w66St8UkCw",
        },
      },
      holder: "did:iota:GJo2HnpjhR75Ty25v38fZ3LCTyXU5gux8Prd9KYmus4N",
      proof: {
        type: "JcsEd25519Signature2020",
        verificationMethod: "#authentication",
        signatureValue:
          "29FwfxAYDpf1CEHSQf56KxBNpytv4z56sJTbouKVLv5kGbrNCkvbmrN6HXi7U3DbfvDFQ34QgCMqpACvBNjWSUcT",
      },
    };

    const result = await check(verifiablePresentation);

    expect(result.verified).toBe(true);
  });
});
